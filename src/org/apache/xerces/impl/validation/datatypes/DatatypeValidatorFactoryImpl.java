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
 * 
 * This class implements a factory of Datatype Validators. Internally the
 * DatatypeValidators are kept in a registry.<BR>
 * There is one instance of DatatypeValidatorFactoryImpl per Parser.<BR>
 * There is one datatype Registry per instance of DatatypeValidatorFactoryImpl,
 * such registry is first allocated with the number DatatypeValidators needed.<BR>
 * e.g.
 * If Parser finds an XML document with a DTD, a registry of DTD validators (only
 * 9 validators) get initialized in the registry.
 * The initialization process consist of instantiating the Datatype and
 * facets and registering the Datatype into registry table.
 * This implementatio uses a Hahtable as a registry table but future implementation
 * should use a lighter object, maybe a Map class ( not use a derived Map class
 * because of JDK 1.1.8 no supporting Map).<BR>
 * <BR>
 * As the Parser parses an instance document it knows if validation needs
 * to be checked. If no validation is necesary we should not instantiate a
 * DatatypeValidatorFactoryImpl.<BR>
 * If validation is needed, we need to instantiate a DatatypeValidatorFactoryImpl.<BR>
 * 
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class DatatypeValidatorFactoryImpl implements DatatypeValidatorFactory {
    private static final boolean   fDebug = false;
    //private Hashtable fgBaseTypes = new Hashtable();
    //private boolean   fRegistryExpanded = false;

    protected static final Hashtable fgBaseTypes = new Hashtable();
    protected static boolean fgRegistryExpanded = false;

    protected Hashtable fUserDefinedTypes = new Hashtable();

    /***
    private static DatatypeValidatorFactoryImpl    fRegistryOfDatatypes = 
    new DatatypeValidatorFactoryImpl();//comment this in when switching to no Singleton

    public DatatypeValidatorFactoryImpl() {
    }
    /***/

    /* comment this out when change to no singleton.
    public  DatatypeValidatorFactoryImpl() {
    }
    */


    //Register Primitive Datatypes

    public void expandRegistryToFullSchemaSet() {

        //Register Primitive Datatypes 
        if (fgRegistryExpanded == false) {
            fgRegistryExpanded = true;
            DatatypeValidator v;
            try {
                fgBaseTypes.put("string",            new StringDatatypeValidator() );
                fgBaseTypes.put("boolean",           new BooleanDatatypeValidator()  );
                fgBaseTypes.put("float",             new FloatDatatypeValidator());
                fgBaseTypes.put("double",            new DoubleDatatypeValidator());
                fgBaseTypes.put("decimal",           new DecimalDatatypeValidator());
                fgBaseTypes.put("timeDuration",      new TimeDurationDatatypeValidator());
                fgBaseTypes.put("recurringDuration", new RecurringDurationDatatypeValidator());
                fgBaseTypes.put("binary",            new BinaryDatatypeValidator());
                fgBaseTypes.put("uriReference",      new URIReferenceDatatypeValidator());
                fgBaseTypes.put("ID",                new IDDatatypeValidator());
                fgBaseTypes.put("IDREF",             new IDREFDatatypeValidator());
                fgBaseTypes.put("ENTITY",            new ENTITYDatatypeValidator());
                fgBaseTypes.put("NOTATION",          new NOTATIONDatatypeValidator());
                fgBaseTypes.put("QName",             new QNameDatatypeValidator()); 


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
            } catch (InvalidDatatypeFacetException ex) {
                ex.printStackTrace();
            } catch (InvalidDatatypeValueException ex) {
                ex.printStackTrace();
            }
        }
    }



    /**
     * Initializes registry with primitive and derived
     * Simple types.
     * 
     * This method does not clear the registry to clear
     * the registry you have to call resetRegistry.
     * 
     * The net effect of this method is to start with
     * a the smallest set of datatypes needed by the
     * validator.
     * 
     * If we start with DTD's then we initialize the
     * table to only the 9 validators needed by DTD Validation.
     * 
     * If we start with Schema's then we initialize to
     * to full set of validators.
     * 
     * @param registrySet
     */
    public void initializeDTDRegistry() {

        if (fgRegistryExpanded == false) { //Core datatypes shared by DTD attributes and Schema

            try {
                fgBaseTypes.put("string",            new StringDatatypeValidator() );
                fgBaseTypes.put("ID",                new IDDatatypeValidator());
                fgBaseTypes.put("IDREF",             new IDREFDatatypeValidator());
                fgBaseTypes.put("ENTITY",            new ENTITYDatatypeValidator());
                fgBaseTypes.put("NOTATION",          new NOTATIONDatatypeValidator());

                createDatatypeValidator( "IDREFS", new IDREFDatatypeValidator(), null , true );

                createDatatypeValidator( "ENTITIES", new ENTITYDatatypeValidator(),  null, true );

                Hashtable facets = new Hashtable();
                facets.put(SchemaSymbols.ELT_PATTERN , "\\c+" );
                createDatatypeValidator("NMTOKEN", new StringDatatypeValidator(), facets, false );

                createDatatypeValidator("NMTOKENS",  
                                        getDatatypeValidator( "NMTOKEN" ), null, true );
            } catch (InvalidDatatypeFacetException ex) {
                ex.printStackTrace();
            } catch (InvalidDatatypeValueException ex) {
                ex.printStackTrace();
            }
        }
    }


    public void resetRegistry() {
        if (fgBaseTypes != null) {
            fgBaseTypes.clear();
            fgRegistryExpanded = false;
            //initializeDTDRegistry();
        }
    }

    /**
     * Create datatype validator.
     * 
     * @param name Name of type.
     * @param base The base type, or null if there is no base type.
     * @param facets The facets.
     * @param list True if this type is a list.
     * 
     * @return The new datatype validator.
     */
    public DatatypeValidator createDatatypeValidator(String name, DatatypeValidator base, Hashtable facets, boolean list)
    throws InvalidDatatypeFacetException, InvalidDatatypeValueException {

        DatatypeValidator simpleType = null;

        if (this.fDebug == true) {
            System.out.println("type name = " + name );
        }

        if (base == null) {
            synchronized (DatatypeValidatorFactoryImpl.class) {
                if (!fgBaseTypes.containsKey(name)) {
                    // lazily construct DTD base types
                    if (!fgBaseTypes.containsKey("NMTOKEN")) {
                        initializeDTDRegistry();
                    }
                    if (!fgBaseTypes.containsKey(name)) {
                        // lazily construct Schema base types
                        if (!fgBaseTypes.containsKey("boolean")) {
                            expandRegistryToFullSchemaSet();
                        }
                    }
                }
            }
            base = getDatatypeValidator(name);
        }
        if (base != null) {
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
            if (fDebug) {
                e.printStackTrace();
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            if (fDebug) {
                e.printStackTrace();
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            if (fDebug) {
                e.printStackTrace();
            } else {
                return null;
            }
        } catch (InvocationTargetException e) {
            if (fDebug) {
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
        DatatypeValidator validator = (DatatypeValidator)fUserDefinedTypes.get(type);
        if (validator == null) {
            validator = (DatatypeValidator)fgBaseTypes.get(type);
        }
        return validator;
    }

    private void addValidator(String name, DatatypeValidator v) {
        fUserDefinedTypes.put(name,v);
    }

    /***
    static public DatatypeValidatorFactoryImpl getDatatypeRegistry() {

        return fRegistryOfDatatypes;
    }//this method needs to be deleted or commented in once we change to no Singleton

    static public void main( String argv[] ) {
        DatatypeValidatorFactoryImpl  tstRegistry = DatatypeValidatorFactoryImpl.getDatatypeRegistry();//this needs to ne commented out or deleted once we go to no singleton
        // This needs to be commented out
        //DatatypeValidatorFactoryImpl  tstRegistry = new DatatypeValidatorFactoryImpl();

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
        while (listOfDatatypes.hasMoreElements()) {
            System.out.println( "Datatype[ " + (index++) + "] =" + listOfDatatypes.nextElement() ); 
        }
        String value = "3.444";
        try {
            tstData4.validate( value, null ); 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Value = " + value + " is valid " ); 


        value = "b344.3";
        try {
            tstData4.validate( value, null ); 
        } catch (Exception ex) {
            System.out.println("float value = " + value + " is Not valid " );
        }

        DatatypeValidator  idData = tstRegistry.getDatatypeValidator( "ID" );

        if (idData != null) {
            ((IDDatatypeValidator) idData).initialize(null);
            try {
                idData.validate( "a1", null );
                idData.validate( "a2", null );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Hashtable tst = (Hashtable)((IDDatatypeValidator) idData).getInternalStateInformation();
            if (tst != null) {
                System.out.println("Table of ID = " + tst.toString());
            }
            //try {
            //   idData.validate( "a1", null );
            //} catch ( Exception ex ) {
            //   ex.printStackTrace();// Should throw a unique exception
            //}

        }

        DatatypeValidator idRefData = tstRegistry.getDatatypeValidator("IDREF" );
        if (idRefData != null) {
            IDREFDatatypeValidator refData = (IDREFDatatypeValidator) idRefData;
            refData.initialize( ((IDDatatypeValidator) idData).getInternalStateInformation());
            try {
                refData.validate( "a1", null );
                refData.validate( "a2", null );
                //refData.validate( "a3", null );//Should throw exception at validate()
                refData.validate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Grammar grammar             = new Grammar();
        if (grammar != null) {
            ENTITYDatatypeValidator entityValidator
            = (ENTITYDatatypeValidator) tstRegistry.getDatatypeValidator("ENTITY");
            entityValidator.initialize( grammar );
            try {
                entityValidator.validate( "a1", null );//Should throw exception
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
    /***/

}


