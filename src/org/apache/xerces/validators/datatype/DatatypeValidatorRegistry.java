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

package org.apache.xerces.validators.datatype;
import java.util.Hashtable;
import org.apache.xerces.validators.datatype.*;
import org.apache.xerces.validators.schema.SchemaSymbols;




/**
 * @version $Id
 * @author  Jeffrey Rodriguez
 */
public class DatatypeValidatorRegistry {
    private static DatatypeValidatorRegistry _instance = new DatatypeValidatorRegistry();
    private Hashtable fRegistry = new Hashtable();

    private DatatypeValidatorRegistry() {
        initializeRegistry();
    }

    /**
     * Initializes registry with primitive and derived
     * Simple types.
     */
    void initializeRegistry() {
        DatatypeValidator  v = null;

        //Register Primitive Datatypes 

        fRegistry.put("string",            new StringValidator() );
        fRegistry.put("boolean",           new BooleanValidator()  );
        fRegistry.put("float",             new FloatValidator());
        fRegistry.put("double",            new DoubleValidator());
        fRegistry.put("decimal",           new DecimalValidator());
        fRegistry.put("timeDuration",      new TimeDurationValidator());
        fRegistry.put("recurringDuration", new RecurringDurationValidator());
        fRegistry.put("binary",            new BinaryValidator());
        fRegistry.put("uriReference",      new URIReferenceValidator());
        fRegistry.put("QName",             new QNameValidator());

        //Register Derived Datatypes

        Hashtable facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*" );
        addValidator("language", createDatatypeValidator("String", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_PATTERN , "\\i\\c*" );
        addValidator("Name", createDatatypeValidator("String", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_PATTERN , "[\\i-[:]][\\c-[:]]*"  );
        addValidator("NCName", createDatatypeValidator("String", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_SCALE, "0");
        addValidator("integer", createDatatypeValidator("decimal", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "0" );
        addValidator("nonPositiveInteger", createDatatypeValidator("integer", facets )); 


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "-1" );
        addValidator("negativeInteger", createDatatypeValidator("nonPositiveInteger", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
        addValidator("long", createDatatypeValidator("integer", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
        addValidator("int", createDatatypeValidator("long", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
        addValidator("short", createDatatypeValidator("int", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
        addValidator("byte",  createDatatypeValidator("short", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
        addValidator("nonNegativeInteger", createDatatypeValidator("integer", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
        addValidator("unsignedLong", createDatatypeValidator("nonNegativeInteger", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
        addValidator("unsignedInt", createDatatypeValidator("unsignedLong", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
        addValidator("unsignedShort", createDatatypeValidator("unsignedInt", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
        addValidator("unsignedByte", createDatatypeValidator("unsignedShort", facets ));


        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "P0Y" );
        facets.put(SchemaSymbols.ELT_PERIOD,   "P0Y" );
        addValidator("timeInstant", createDatatypeValidator("recurringDuration", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "P0Y" );
        facets.put(SchemaSymbols.ELT_PERIOD,   "PY24H" );
        addValidator("time", createDatatypeValidator("recurringDuration", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_PERIOD,   "P0Y" );
        addValidator("timePeriod", createDatatypeValidator("recurringDuration", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "PT24H" );
        addValidator("date", createDatatypeValidator("timePeriod", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "P1M" );
        addValidator("month", createDatatypeValidator("timePeriod", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "P1Y" );
        addValidator("year", createDatatypeValidator("timePeriod", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_DURATION, "P100Y" );
        addValidator("century", createDatatypeValidator("timePeriod", facets ));

        facets = new Hashtable();
        facets.put(SchemaSymbols.ELT_PERIOD, "P1Y" );
        facets.put(SchemaSymbols.ELT_DURATION, "PT24H" );
        addValidator("recurringDate", createDatatypeValidator("recurringDuration", facets ));
    }

    public DatatypeValidator createDatatypeValidator(String baseTypeName, Hashtable facets ){
        DatatypeValidator  baseDatatype  = getDatatypeValidator( baseTypeName );
        try {
            if( baseDatatype != null ) {
                baseDatatype.setFacets(facets);
                baseDatatype.setBasetype( baseTypeName );
            }
        } catch (IllegalFacetException ex) {
            ex.printStackTrace();
        } catch (IllegalFacetValueException ex) {
            ex.printStackTrace();
        } catch (UnknownFacetException ex) {
            ex.printStackTrace();
        }
        return baseDatatype;
    }

    public DatatypeValidator getDatatypeValidator(String type) {
        DatatypeValidator simpleType = null;
        if( fRegistry.containsKey( type ) == true ){
            simpleType = (DatatypeValidator) fRegistry.get(type);
            if( simpleType != null ) {
                try {
                simpleType = (DatatypeValidator) simpleType.getClass().newInstance(); 
                } catch( InstantiationException e ) {
                    e.printStackTrace();
                } catch( IllegalAccessException e ) {
                    e.printStackTrace();
                }
            }
        }
        return simpleType;
    }

    public void addValidator(String name, DatatypeValidator v) {
       fRegistry.put(name,v);
   }

    static public DatatypeValidatorRegistry getDatatypeRegistry() {
         return _instance;
    }

}

