/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.validation.datatypes;

import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.reflect.*;
import org.apache.xerces.impl.validation.DatatypeValidator;
import org.apache.xerces.impl.validation.DatatypeValidatorFactory;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;
import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
import org.apache.xerces.impl.validation.Grammar;



/**
 * This is the datatype validator factory registry.
 * 
 * This class is based on a Singleton pattern so there
 * only one registry table, fBaseTypes.
 * 
 * This class implements a factory method and it is
 * use by validators to register and obtain datatypes.
 * 
 * Validator should first obtain the datatype Registry
 * through the getDatatypeRegistry class method, and
 * then they should call the createDatatypeValidator
 * method to get a validator.
 * 
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class DatatypeValidatorFactoryImpl implements DatatypeValidatorFactory {
   private static final boolean                   fDebug               = false;
   private Hashtable fBaseTypes = new Hashtable();


   private static DatatypeValidatorFactoryImpl    fRegistryOfDatatypes = 
   new DatatypeValidatorFactoryImpl();

   private DatatypeValidatorFactoryImpl() {
      initializeRegistry();
   }

   /**
    * Initializes registry with primitive and derived
    * Simple types.
    */
   void initializeRegistry() {
      DatatypeValidator  v = null;

      //Register Primitive Datatypes 

      try {
         fBaseTypes.put("string",            new StringDatatypeValidator() );
         fBaseTypes.put("boolean",           new BooleanDatatypeValidator()  );
         fBaseTypes.put("float",             new FloatDatatypeValidator());
         fBaseTypes.put("double",            new DoubleDatatypeValidator());
         fBaseTypes.put("decimal",           new DecimalDatatypeValidator());
         fBaseTypes.put("timeDuration",      new TimeDurationDatatypeValidator());
         fBaseTypes.put("recurringDuration", new RecurringDurationDatatypeValidator());
         fBaseTypes.put("binary",            new BinaryDatatypeValidator());
         fBaseTypes.put("uriReference",      new URIReferenceDatatypeValidator());
         fBaseTypes.put("ID",                new IDDatatypeValidator());
         fBaseTypes.put("IDREF",             new IDREFDatatypeValidator());
         fBaseTypes.put("ENTITY",            new ENTITYDatatypeValidator());
         fBaseTypes.put("NOTATION",          new NOTATIONDatatypeValidator());
         fBaseTypes.put("QName",             new QNameDatatypeValidator()); 


         Hashtable facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*" );

         createDatatypeValidator("language", new StringDatatypeValidator() , facets,
                                 false );

         createDatatypeValidator( "IDREFS", new IDREFDatatypeValidator(), null , true );

         createDatatypeValidator( "ENTITIES", new ENTITYDatatypeValidator(),  null, true );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PATTERN , "\\c+" );
         createDatatypeValidator("NMTOKEN", new StringDatatypeValidator(), facets, false );

         createDatatypeValidator("NMTOKENS",  
                                 getDatatypeValidator( "NMTOKEN" ), null, true );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PATTERN , "\\i\\c*" );
         createDatatypeValidator("Name", new StringDatatypeValidator(), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PATTERN , "[\\i-[:]][\\c-[:]]*"  );
         createDatatypeValidator("NCName", new StringDatatypeValidator(), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_SCALE, "0");
         createDatatypeValidator("integer", new DecimalDatatypeValidator(), facets, false);


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "0" );
         createDatatypeValidator("nonPositiveInteger", 
                                 getDatatypeValidator("integer"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "-1" );
         createDatatypeValidator("negativeInteger", 
                                 getDatatypeValidator( "nonPositiveInteger"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
         facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
         createDatatypeValidator("long", getDatatypeValidator( "integer"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
         facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
         createDatatypeValidator("int", getDatatypeValidator( "long"), facets,false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
         facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
         createDatatypeValidator("short", getDatatypeValidator( "int"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
         facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
         createDatatypeValidator("byte",
                                 getDatatypeValidator( "short"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
         createDatatypeValidator("nonNegativeInteger", 
                                 getDatatypeValidator( "integer"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
         createDatatypeValidator("unsignedLong",
                                 getDatatypeValidator( "nonNegativeInteger"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
         createDatatypeValidator("unsignedInt",
                                 getDatatypeValidator( "unsignedLong"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
         createDatatypeValidator("unsignedShort", 
                                 getDatatypeValidator( "unsignedInt"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
         createDatatypeValidator("unsignedByte",
                                 getDatatypeValidator( "unsignedShort"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_MININCLUSIVE, "1" );
         createDatatypeValidator("positiveInteger",
                                 getDatatypeValidator( "nonNegativeInteger"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "P0Y" );
         facets.put(SchemaSymbols.ELT_PERIOD,   "P0Y" );
         createDatatypeValidator("timeInstant", 
                                 getDatatypeValidator( "recurringDuration"),facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "P0Y" );
//            facets.put(SchemaSymbols.ELT_PERIOD,   "PY24H" ); Bug -- WORK TODO
         createDatatypeValidator("time", 
                                 getDatatypeValidator( "recurringDuration"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PERIOD,   "P0Y" );
         createDatatypeValidator("timePeriod", 
                                 getDatatypeValidator( "recurringDuration"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "PT24H" );
         createDatatypeValidator("date",
                                 getDatatypeValidator( "timePeriod"), facets, false );


         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "P1M" );
         createDatatypeValidator("month",
                                 getDatatypeValidator( "timePeriod"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "P1Y" );
         createDatatypeValidator("year", 
                                 getDatatypeValidator( "timePeriod"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_DURATION, "P100Y" );
         createDatatypeValidator("century", 
                                 getDatatypeValidator( "timePeriod"), facets, false );

         facets = new Hashtable();
         facets.put(SchemaSymbols.ELT_PERIOD, "P1Y" );
         facets.put(SchemaSymbols.ELT_DURATION, "PT24H" );
         createDatatypeValidator("recurringDate",
                                 getDatatypeValidator( "recurringDuration"),facets, false );
      } catch ( InvalidDatatypeFacetException ex ) {
         ex.printStackTrace();
      } catch ( InvalidDatatypeValueException ex ) {
         ex.printStackTrace();
      }
   }

   public void resetRegistry(){
      fBaseTypes.clear();
      initializeRegistry();
   }

   public DatatypeValidator createDatatypeValidator(String name, DatatypeValidator base, Hashtable facets, boolean list)
   throws InvalidDatatypeFacetException, InvalidDatatypeValueException {

      DatatypeValidator simpleType = null;

      if (this.fDebug == true) {
         System.out.println("type name = " + name );
      }

      if ( base != null ) {
         if (list) {
            simpleType = new ListDatatypeValidator(base, facets, list);    
         } else {
            try {
               Class validatorDef = base.getClass();

               Class [] validatorArgsClass = new Class[] {  
                  org.apache.xerces.impl.validation.DatatypeValidator.class,
                  java.util.Hashtable.class,
                  boolean.class};



               Object [] validatorArgs     = new Object[] {
                  base, facets, Boolean.FALSE};




               Constructor validatorConstructor =
               validatorDef.getConstructor( validatorArgsClass );


               simpleType = 
               ( DatatypeValidator ) createDatatypeValidator (
                                                             validatorConstructor, validatorArgs );
            } catch (NoSuchMethodException e) {
               e.printStackTrace();
            }
         }

         if (simpleType != null) {
            addValidator( name, simpleType );//register validator
         }


      }

      return simpleType;// return it
   }



   private static Object createDatatypeValidator(Constructor validatorConstructor, 
                                                 Object[] arguments)  throws  InvalidDatatypeFacetException {
      Object validator = null;
      try {
         validator = validatorConstructor.newInstance(arguments);
         return validator;
      } catch (InstantiationException e) {
         if ( fDebug ) {
            e.printStackTrace();
         } else {
            return null;
         }
      } catch (IllegalAccessException e) {
         if ( fDebug ) {
            e.printStackTrace();
         } else {
            return null;
         }
      } catch (IllegalArgumentException e) {
         if ( fDebug ) {
            e.printStackTrace();
         } else {
            return null;
         }
      } catch (InvocationTargetException e) {
         if ( fDebug ) {
            System.out.println("!! The original error message is: " + e.getTargetException().getMessage() );
            e.getTargetException().printStackTrace();
         } else {
            throw new InvalidDatatypeFacetException( e.getTargetException().getMessage() );
            //System.out.println("Exception: " + e.getTargetException
            //validator = null;
         }
      }
      return validator;
   }


   public DatatypeValidator getDatatypeValidator(String type) {
      AbstractDatatypeValidator simpleType = null;
      if ( fDebug ) {
         System.out.println( "type = >" + type +"<");
         System.out.println( "fBaseTypes = >" + fBaseTypes +"<" );
         simpleType = (AbstractDatatypeValidator) fBaseTypes.get(type);
      }
      if ( type != null && fBaseTypes != null
           && fBaseTypes.containsKey( type ) == true ) {
         simpleType = (AbstractDatatypeValidator) fBaseTypes.get(type);
      }
      return(DatatypeValidator) simpleType;
   }

   private void addValidator(String name, DatatypeValidator v) {
      fBaseTypes.put(name,v);
   }

   static public DatatypeValidatorFactoryImpl getDatatypeRegistry()  {

      return fRegistryOfDatatypes;
   }

   static public void main( String argv[] ){
      DatatypeValidatorFactoryImpl  tstRegistry = DatatypeValidatorFactoryImpl.getDatatypeRegistry();

      System.out.println("tstRegistry = " + tstRegistry );

      DatatypeValidator   tstData1            = tstRegistry.getDatatypeValidator( "string" );
      DatatypeValidator   tstData2            = tstRegistry.getDatatypeValidator( "boolean" );
      DatatypeValidator   tstData3            = tstRegistry.getDatatypeValidator( "NOTATION" );
      DatatypeValidator   tstData4            = tstRegistry.getDatatypeValidator( "float" );

      System.out.println( "string = " + tstData1 );
      System.out.println( "boolean = " + tstData2 );
      System.out.println( "NOTATION = " + tstData3 );

      System.out.println( "registry = " + tstRegistry );
      System.out.println( "basetypes registered are " );
      Enumeration  listOfDatatypes = tstRegistry.fBaseTypes.keys();
      int index = 0;
      while ( listOfDatatypes.hasMoreElements() ) {
         System.out.println( "Datatype[ " + (index++) + "] =" + listOfDatatypes.nextElement() ); 
      }
      String value = "3.444";
      try {
         tstData4.validate( value, null ); 
      } catch ( Exception ex ) {
         ex.printStackTrace();
      }
      System.out.println("Value = " + value + " is valid " ); 


      value = "b344.3";
      try {
         tstData4.validate( value, null ); 
      } catch ( Exception ex ) {
         System.out.println("float value = " + value + " is Not valid " );
      }

      DatatypeValidator  idData = tstRegistry.getDatatypeValidator( "ID" );

      if (  idData != null ) {
         ((IDDatatypeValidator) idData).initialize();
         try {
            idData.validate( "a1", null );
            idData.validate( "a2", null );
         } catch ( Exception ex ) {
            ex.printStackTrace();
         }
         Hashtable tst = (Hashtable)((IDDatatypeValidator) idData).getTableIds();
         if (tst != null) {
            System.out.println("Table of ID = " + tst.toString());
         }
         /*
         try {
            idData.validate( "a1", null );
         } catch ( Exception ex ) {
            ex.printStackTrace();// Should throw a unique exception
         }
         */

      }

      DatatypeValidator idRefData = tstRegistry.getDatatypeValidator("IDREF" );
      if( idRefData != null ){
         IDREFDatatypeValidator refData = (IDREFDatatypeValidator) idRefData;
         refData.initialize( ((IDDatatypeValidator) idData).getTableIds());
         try {
            refData.validate( "a1", null );
            refData.validate( "a2", null );
            //refData.validate( "a3", null );//Should throw exception at validate()
            refData.validate();
         } catch( Exception ex ){
            ex.printStackTrace();
         }
      }
     Grammar grammar             = new Grammar();
     if( grammar != null ){
        ENTITYDatatypeValidator entityValidator
                 = (ENTITYDatatypeValidator) tstRegistry.getDatatypeValidator("ENTITY");
        entityValidator.initialize( grammar );
        try {
           entityValidator.validate( "a1", null );//Should throw exception
        } catch( Exception ex ){
           ex.printStackTrace();
        }
     }

   }

}

