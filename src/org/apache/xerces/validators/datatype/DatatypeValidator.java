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

package org.apache.xerces.validators.datatype;

import java.util.Hashtable;
import java.util.Locale;

/**
 * DataTypeValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * Note: there is no support for facets in this API, since we are trying to convince
 * W3C to remove facets from the data type spec.
 * 
 * @author Jeffrey Rodriguez-
 * @version $Id$
 */
public interface DatatypeValidator {
    public static final short FACET_LENGTH       = 0x1<<0;
    public static final short FACET_MINLENGTH    = 0x1<<1;
    public static final short FACET_MAXLENGTH    = 0x1<<2;
    public static final short FACET_PATTERN      = 0x1<<3; 
    public static final short FACET_ENUMERATION  = 0x1<<4;
    public static final short FACET_MAXINCLUSIVE = 0x1<<5;
    public static final short FACET_MAXEXCLUSIVE = 0x1<<6;
    public static final short FACET_MININCLUSIVE = 0x1<<7;
    public static final short FACET_MINEXCLUSIVE = 0x1<<8;
    public static final short FACET_TOTALDIGITS  = 0x1<<9;
    public static final short FACET_FRACTIONDIGITS = 0x1<<10;
    public static final short FACET_ENCODING     = 0x1<<11;
    public static final short FACET_DURATION     = 0x1<<12;
    public static final short FACET_PERIOD       = 0x1<<13;
    public static final short FACET_WHITESPACE   = 0x1<<14;

    // _dummy_ facet that is passed with all other facets for a datatype
    // its value flags the fixed facets
    public static final String FACET_FIXED   = "fixed";
    
    //2.4.2.6 whiteSpace - Datatypes 
    public static final short PRESERVE = 0;
    public static final short REPLACE  = 1;
    public static final short COLLAPSE = 2;



    /**
     * Checks that "content" string is valid 
     * datatype.
     * If invalid a Datatype validation exception is thrown.
     * 
     * @param content A string containing the content to be validated
     *                
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see         org.apache.xerces.validators.datatype.InvalidDatatypeValueException
     */
    public Object validate(String content, Object state ) throws InvalidDatatypeValueException;

    public short getWSFacet ();
    
    // returns the base datatypeValidator of the current validator.  
    public DatatypeValidator getBaseValidator();

    /**
     * Compares content in the Domain value vs. lexical
     * value.
     * e.g. If type is a float then 1.0 may be equivalent
     * to 1 even tough both are lexically different.
     * 
     * @param value1
     * @param valu2
     * @return 
     */
    public int compare( String value1, String value2);

}
