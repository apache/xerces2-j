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

package org.apache.xerces.impl.validation.datatypes.eTypes.xml;


import org.apache.xerces.impl.validation.datatypes.eTypes.Data.BasicStringProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.Helpers;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.Property;
import org.apache.xerces.impl.validation.datatypes.eTypes.xml.XMLStringIF;

import java.lang.RuntimeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.Boolean;
import java.util.StringTokenizer;
import java.lang.StringBuffer;


/**
 * Implements XMLStringIF functions
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public  class XMLString extends BasicStringProperty implements XMLStringIF {
   final public static int ENUM = BasicStringProperty.classNumberSubProperties + XMLStringIF.ENUM;
   final public static int NAME = BasicStringProperty.classNumberSubProperties + XMLStringIF.NAME;
   final public static int PLURAL = BasicStringProperty.classNumberSubProperties + XMLStringIF.PLURAL;
   final public static int classNumberSubProperties = 
   BasicStringProperty.classNumberSubProperties + XMLStringIF.classNumberSubProperties;
   {
      setSubPropName( PLURAL, "XMLString.PLURAL" );
      setSubPropName( NAME, "XMLString.NAME" );
      setSubPropName( ENUM, "XMLString.ENUM"  );
   }
   public static final Method isEnum;
   public static final Method isNm;
   public static final Method pluralRequired;
   static {
      try {
         isEnum = XMLString.class.getMethod("isEnum",new Class[]{Object.class,Object.class});
         isNm = XMLString.class.getMethod("isNm",new Class[]{Object.class,Object.class});
         pluralRequired = XMLString.class.getMethod("pluralRequired",new Class[]{Object.class,Object.class});
      } catch (Exception e) {
         throw new RuntimeException("One (or more) of static methods missing from XMLString found");
      }
   }
   private Method method = null;
   private String[] enumValues = new String[]{"","",""};

   static String languagePattern;
   static String NCNamePattern = "^[^:]*$";
   /** Note that QNames may contain 0 or 1 ':'s */
   static String QNamePattern = "^(?:[^:]*:)?[^:]*$";
   static {
      String $Subcode    = "(?:-[A-z]+)";

      String $ISO639Code = "(?:[A-z][A-z])";
      String $IanaCode = "(?:i-[A-z]+)";
      String $UserCode = "(?:x-[A-z]+)";


      String $langCode = "(?:" + $ISO639Code + "|" + $IanaCode + "|" +$UserCode + ")";
      languagePattern = "^" + $langCode  + $Subcode   + "*$";
      //languagePattern = "^(?:(?:i-[A-z]+)|(?:[A-z][A-z])|(?:x-[A-z]+))(?:-[A-z]+)$";

   }
   public final static String xmlWhitespace = "\n\r\t ";

   //public static final Vector xmlStringTypes = Perl . a2v(new String[] {
   //                                                          "string", "language", "NAMES",  "QName", "NCName",  "NMTOKENS", "PLURAL"
   //                                                       });


   public static final Vector xmlStringTypes = new Vector();

   public XMLString() {
      super();
   }
   public XMLString(String str) {
      super(str);
   }
   /**
       If called with name == "XMLString.PLURAL" the result of accumulate will be
       non-null if at least one of the instances has more than 1 token.  If called with
       "SINGULAR" it will be non-null if each instance is atomic, not a list.
*/
   public void enableSubProp( String name ){
      int index = getSubPropNameIndex( name );
      if ( index == ENUM ) {
         setSubPropEval( index, isEnum );
         setSubPropMerge( index, Helpers . andMethod );
      } else if ( index == PLURAL ) {
         setSubPropEval( index, pluralRequired);
         setSubPropMerge( index, Helpers . orMethod );
      } else if ( index == NAME ) {
         setSubPropEval( index, isNm);
         setSubProp( "NAME" , NAME , Property . constraint );
         setSubPropMerge( index, Helpers . andMethod );
      } else if ( name . equals( "SINGULAR" ) ) {
         setSubPropEval( index, pluralRequired);
         setSubPropMerge( index, Helpers . allFalse );                  
      } else {
         super . enableSubProp( name );
      }
   }
   public int getLength(){
      if ( getSValue() == null ) {
         return -1;
      }
      return getSValue() . length();
   }
   /** Convenience function for !getSubProp( ParitalOrder.CLOSED_ABOVE , Property.instance ) */
   final public boolean getMaxExclusive(){
      return !((Boolean)getSubProp( CLOSED_ABOVE , Property.constraint )).booleanValue();
   }
   /** Convenience function for getSubProp( ParitalOrder.CLOSED_ABOVE , Property.instance ) */
   final public boolean getMaxInclusive(){
      return((Boolean)getSubProp( CLOSED_ABOVE , Property.constraint )).booleanValue();
   }
   /** Convenience function for getSubProp( StringProperty.MAX_LEN , Property.constraint ) */
   final public int getMaxLength(){
      return((Integer)getSubProp( BasicStringProperty.MAX_LEN , Property.constraint )).intValue();
   }
   /** Convenience function for !getSubProp( ParitalOrder.CLOSED_BELOW , Property.instance ) */
   final public boolean getMinExclusive(){
      return ! ((Boolean)getSubProp( CLOSED_BELOW , Property.constraint )).booleanValue();
   }
   /** Convenience function for getSubProp( ParitalOrder.CLOSED_BELOW , Property.instance ) */
   final public boolean getMinInclusive(){
      return((Boolean)getSubProp( CLOSED_BELOW , Property.constraint )).booleanValue();
   }
   /** Convenience function for getSubProp( StringProperty.MIN_LEN , Property.constraint ) */
   final public int getMinLength(){
      return((Integer)getSubProp( BasicStringProperty.MIN_LEN , Property.constraint )).intValue();
   }
   final public static Boolean isEnum(Object obj, Object constraint){
      String s;
      if ( obj == null || ! ( obj instanceof String ) ) {
         return null;
      } else {
         s = ((String) obj) . trim();
      }
      if ( ( (String) constraint ) . indexOf( s ) < 0 ) {
         return null;
      }
      return Boolean.TRUE;
   }
   /** Convenience function for getSubProp( ENUM , Property.constraint ) */
   public boolean isEnumeration(){
      return  getSubProp( ENUM , Property.constraint ) != null ;
   }
   /** Splits obj into space separated tokens, and returns true if they are each NAME
   or NMTOKEN as requested */
   final public static Boolean isNm(Object obj, Object constraint){
      String s;
      if ( obj == null || ! ( obj instanceof String ) ) {
         return null;
      } else {
         s = ( (String) obj ) . trim();
      }
      if ( s . length() == 0 ) {
         return null;
      }
      StringTokenizer st = new StringTokenizer(s, xmlWhitespace);
      final boolean NMTOKENrequested = 
      ( constraint instanceof String ) && ( "NMTOKEN". equals ((String)constraint) ) ;
      while ( st . hasMoreElements() ) {
         s = (String) st . nextElement();
         if ( s . length() == 0 ) {
            return null;
         }
         if ( NMTOKENrequested ) {
            /*
            if ( ! XmlStringRecognizer . isName( "A" + s ) ) {
               return null;
            }
            */
         } else {
            /*
            if (! XmlStringRecognizer . isName( s )) {
               return null;
            }
            */
         }
      }
      return Boolean.TRUE;
   }
   final public static Boolean pluralRequired(Object obj, Object constraint){
      String s;
      if ( ! ( obj instanceof String ) ) {
         return null;
      } else {
         s = (String) obj;
      }
      if ( s == null ) {
         return null;
      }
      StringTokenizer st = new StringTokenizer( s , xmlWhitespace );
      if ( ! st . hasMoreElements() ) {
         return null;
      }
      st . nextElement();
      return( st . hasMoreElements() ) ? Boolean.TRUE : null;
   }
   /** Convenience method.  token in values must be name tokens */
   final public void setEnumeration(String values){
      StringBuffer sb = new StringBuffer(";");
      if ( values == null || values.trim().length() == 0 ) {
         setSubProp( null , ENUM , Property.constraint);
         setSubPropEval( ENUM , null );
         setSubPropMerge( ENUM , null );
         return;
      }
      enableSubProp( "XMLString.ENUM" );
      values = values.trim();
      StringTokenizer st = new StringTokenizer( values , xmlWhitespace );
      while ( st . hasMoreElements() ) {
         String str = (String) st . nextElement();
         if ( str . equals("") || !isNm( "A"+str , null ).booleanValue() ) {
            throw new RuntimeException("Attempt to create enumeration with non-NMTOKEN value >" + str + "<\n");
         }
         sb . append( str );
         sb . append( ";" );
      }
      setSubProp( sb . toString() , ENUM , Property.constraint );
   }
   /** Exclusive bounds not supported */
   public void setMaxExclusive(){
      throw new RuntimeException("Exclusive string length bounds not supported (yet) ");
   }
   public void setMaxInclusive(){}
   /** Convenience function for setSubProp( Object, StringProperty.MAX_LEN , Property.constraint ) */
   final public void setMaxLength(int m){
      enableSubProp( "BasicStringProperty.MAX_LEN" );
      setSubProp(  new Integer(m), BasicStringProperty.MAX_LEN , Property.constraint );
   }
   /** Exclusive bounds not supported */
   public void setMinExclusive(){
      throw new RuntimeException("Exclusive string length bounds not supported (yet) ");
   }
   public void setMinInclusive(){}
   /** Convenience function for setSubProp( Object, StringProperty.MIN_LEN , Property.constraint ) */
   final public void setMinLength(int m){
      enableSubProp( "BasicStringProperty.MIN_LEN" );
      setSubProp(  new Integer(m), BasicStringProperty.MIN_LEN , Property.constraint );
   }
   /** Note that validation does not check  global constraints - just syntax of strings */
   public void setXMLStringType(String typeName ) {
      setName( typeName );
      if ( typeName . startsWith( "NAME" ) ) {
         enableSubProp( "XMLString.NAME" );
         setMinLength( 1 );
      } else if ( typeName . startsWith( "NMTOKEN" ) ) {
         enableSubProp( "XMLString.NAME" );
         setSubProp( "NMTOKEN" , NAME , Property . constraint );
         setMinLength( 1 );
      } else if ( typeName . equals( "QName" ) ) {
         enableSubProp( "XMLString.NAME" );
         setPattern( QNamePattern );
      } else if ( typeName . equals( "NCName" ) ) {
         enableSubProp( "XMLString.NAME" );
         setPattern( NCNamePattern );
      } else if ( typeName . equals( "language" ) ) {
         setPattern( languagePattern );
      } else if ( typeName . equals("boolean") ) {
         setEnumeration("true false 0 1");
      } else if ( typeName . equals("PLURAL") ) {
         enableSubProp("XMLString.PLURAL");
      }
   }
   /** Validate sets subProps in the subProp[Property.instance] array , checking it wrt the . 
    subProp[Property.constraint] array */
   public boolean validate(String str) {
      setSValue(str);
      if (!super.validate(str)) {
         return false;
      }
      boolean enum = ((Boolean)getSubProp( ENUM , Property.constraint  )) . booleanValue();
      if (enum) {
         str = str.trim();
         if ( enumValues[Property.constraint.intValue()].indexOf(" " + str + " ") < 0 ) {
            return false; // value not permitted
         }
         if ( enumValues[Property.constraint.intValue()].indexOf(" " + str + " ") < 0 ) {
            enumValues[Property.constraint.intValue()] =
            enumValues[Property.constraint.intValue()] + " " + str;
         }
      }
      if ( method != null )
         try {
            if (!((Boolean) getSubProp( PLURAL , Property.constraint )).booleanValue()) {
               // We have not set PLURAL to FALSE.
               return((Boolean) method.invoke(null, new Object[] {str})).booleanValue();
            } else {
               StringTokenizer st = new StringTokenizer(str, xmlWhitespace);
               if ( ! st . hasMoreElements() ) {
                  return false;
               }
               while (st.hasMoreElements()) {
                  str = (String) st.nextElement();
                  if (st.hasMoreElements()) {
                     return false; // NOT plural
                  }
                  return((Boolean) method.invoke(null, new Object[] {str})).booleanValue();
               }
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Exception thrown" + e);
         } catch (InvocationTargetException f) {
            throw new RuntimeException("Exception thrown" + f);
         } else return true;
      throw new RuntimeException("Shouldn't be here");
   }
}
