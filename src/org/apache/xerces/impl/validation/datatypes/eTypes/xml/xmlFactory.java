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


import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.Property;

import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.PO_IF;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.Helpers;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.Number.mNumber;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime.ISODate;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime.ISOTime;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime.ISODateTime;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime.ISO8601;

import org.apache.xerces.impl.validation.datatypes.eTypes.Data.uri;


import java.lang.CloneNotSupportedException;
import java.util.Enumeration;
import java.util.Hashtable;
/**
 * Supplies objects which will validate the XML Schema built-in types.
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class xmlFactory {
   private static Hashtable ht;
   /** types which can be created using getXMLType( name ) */
   public static final String[] types = new String[] {
      "double" , "float" , "decimal" , "fixed" , "ISODate" , "ISOTime" , "ISODateTime" ,
      "ISO8601" , "uriReference" , "uri" , "positive-integer" , "non-positive-integer" ,
      "negative-integer" , "non-negative-integer" , "unsigned-byte" , "unsigned-short" ,
      "unsigned-int" , "unsigned-long" , "long" , "int" , "short" , "byte" , "string" ,
      "boolean" , "language" , "NMTOKENS" , "NAMES" , "QName" , "NCName", "PLURAL"};


   /** Format is string naming xml-type, followed by two doubles which specify the
         lower and upper bound respectively.  If a double is null, there is no
         restriction on that bound.  Legal types are:
         <p>
         positive-integer non-positive-integer negative-integer non-negative-integer
         unsigned-byte unsigned-short unsigned-int unsigned-long unsigned-long unsigned-int
         unsigned-short unsigned-byte
         <p>
         The values +/- 100e+1000 used below are +/- Infinity.  If we go to unbounded numbers,
         they may need to be changed
   */
   final static public Object[] xmlIntegerTypes = new Object[] { 
      new Object[]{ "positive-integer", new Double(1) , new Double("100e+1000")},
      new Object[]{ "non-positive-integer", new Double("-100e+1000") , new Double(0)},
      new Object[]{ "negative-integer", new Double("-100e+1000") , new Double(-1)},
      new Object[]{ "non-negative-integer",  new Double(0) , new Double("100e+1000")},
      new Object[]{ "unsigned-byte",  new Double(0) , new Double(255)},
      new Object[]{ "unsigned-short",  new Double(0) , new Double(65535)},
      new Object[]{ "unsigned-int",  new Double(0) , new Double(4294967295.0)},
      new Object[]{ "unsigned-long",  new Double(0) , new Double(18446744073709551615.0)},
      new Object[]{ "long",  new Double(-9223372036854775808.0) , new Double(9223372036854775807.0)},
      new Object[]{ "int",  new Double(-2147483648) , new Double(2147483647)},
      new Object[]{ "short",  new Double(-32768) , new Double(32767)},
      new Object[]{ "byte",  new Double(-128) , new Double(127)}
   }; 
   static { foo();}
   /** 
    * Returns a type which will validate one of the following xml-types:
 
    positive-integer non-positive-integer negative-integer non-negative-integer
    unsigned-byte unsigned-short unsigned-int unsigned-long unsigned-long unsigned-int
    unsigned-short unsigned-byte

   */
   private static final Property createXMLIntegerType(String typeName) {
      mNumber result = new mNumber( typeName );
      setXMLIntegerType( result , typeName );
      result . enableSubProp( "mNumber.INTEGRAL" );
      return result;
   }
   private static Property createXMLStringType(String typeName) {

      XMLString result = new XMLString();
      result.setXMLStringType(typeName);

      return result;
   }
   private static void foo() {
      Object obj;
      ht = new Hashtable();
      ht.put("double", obj = new mNumber( "double" ));
      ht.put("float", obj = new mNumber( "float" ));

      ht.put("decimal", obj = new mNumber( "decimal" ));
      ( (mNumber)obj ) . enableSubProp( "mNumber.FIXED" );

      ht.put("fixed", new mNumber( "fixed" ));
      ht.put("ISODate" , new ISODate());
      ht.put("ISOTime" , new ISOTime());
      ht.put("ISODateTime" , new ISODateTime());
      ht.put("ISO8601" , new ISO8601());
      ht.put("uriReference", new uri());
      ht.put("uri", new uri());
      ht.put("positive-integer",
             createXMLIntegerType("positive-integer"));
      ht.put("non-positive-integer",
             createXMLIntegerType("non-positive-integer"));
      ht.put("negative-integer",
             createXMLIntegerType("negative-integer"));
      ht.put("non-negative-integer",
             createXMLIntegerType("non-negative-integer"));
      ht.put("unsigned-byte",
             createXMLIntegerType("unsigned-byte"));
      ht.put("unsigned-short",
             createXMLIntegerType("unsigned-short"));
      ht.put("unsigned-int",
             createXMLIntegerType("unsigned-int"));
      ht.put("unsigned-long",
             createXMLIntegerType("unsigned-long"));
      ht.put("long",
             createXMLIntegerType("long"));
      ht.put("int",
             createXMLIntegerType("int"));
      ht.put("short",
             createXMLIntegerType("short"));
      ht.put("byte",
             createXMLIntegerType("byte"));

      ht.put("string",
             createXMLStringType("string"));
      ht . put("PLURAL" , 
               createXMLStringType("PLURAL"));
      ht . put("boolean",
               createXMLStringType("boolean"));
      ht.put("NAMES",
             createXMLStringType("NAMES"));
      ht.put("language",
             createXMLStringType("language"));

      ht.put("NMTOKENS",
             createXMLStringType("NMTOKENS"));
      ht.put("QName",
             createXMLStringType("QName"));
      ht.put("NCName",
             createXMLStringType("NCName"));
   }
   public static Enumeration getSimpleTypeNames(){
      return ht . keys();
   }
   public static Property getXMLProperty(String str) {
      if (str == null) {
         return null;
      }
      Object obj = ht.get(str);
      Property prop = (Property) obj;
      if (obj == null) {
         return null;
      }
      return(Property) prop . twin( );
   }
   public static Property getXMLType(String str) {
      if (str == null) {
         return null;
      }
      Object obj = ht.get(str);
      Property prop = (Property) obj;
      if (obj == null) {
         return null;
      }
      return(Property) prop . twin( );
   }
   /**
    * Imposes the ranges associated with the following xml built-in derived types with
    anything derived from mNumber.  
    <p>
    positive-integer non-positive-integer negative-integer non-negative-integer
    unsigned-byte unsigned-short unsigned-int unsigned-long unsigned-long unsigned-int
    unsigned-short unsigned-byte
    <p>


    */
   public static void setXMLIntegerType(mNumber num , String type) {
      int i;
      num . setName( type );
      for ( i = 0; i < xmlIntegerTypes.length ; i++ ) {
         Object[] infoArray = ((Object[]) xmlIntegerTypes[i]);
         if ( infoArray[0] . equals( type ) ) {
            num . setBound( PO_IF.below , infoArray[ 1 ] , Property.constraint );
            num . setSubPropEval( PartialOrder.MIN , Helpers.constrainBelowClosed );
            num . setSubPropMerge( PartialOrder.MIN , Helpers.minMethod );

            num . setBound( PO_IF.above , infoArray[ 2 ] , Property.constraint );
            num . setSubPropEval( PartialOrder.MAX , Helpers.constrainAboveClosed );
            num . setSubPropMerge( PartialOrder.MAX , Helpers.maxMethod );

            return;
         }
      }
      throw new RuntimeException("Non-existent type requested: " + type);
   }
}
