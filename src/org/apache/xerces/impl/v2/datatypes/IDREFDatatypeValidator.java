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

package org.apache.xerces.impl.v2.datatypes;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.impl.v2.msg.XMLMessages;
import org.apache.xerces.impl.v2.SchemaSymbols;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.v2.XSMessageFormatter;
import org.apache.xerces.impl.validation.ValidationContext;

/**
 * IDREFValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 *
 * @author Jeffrey Rodriguez-
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class IDREFDatatypeValidator extends StringDatatypeValidator {
    private static Object                   fNullValue      = new Object();

    public static final  int                IDREF_VALIDATE  = 0;
    public static final  int                IDREF_CHECKID   = 1;

    public IDREFDatatypeValidator ()   {
        this( null, null, false, null ); // Native, No Facets defined, Restriction
    }

    public IDREFDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                    boolean derivedByList, XMLErrorReporter reporter) {

        // all facets are handled in StringDatatypeValidator
        super (base, facets, derivedByList, reporter);

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        // the type is NAME by default
        // REVISIT: how to set token type for id/idref derived types
        if (base instanceof IDREFDatatypeValidator)
            setTokenType(((IDREFDatatypeValidator)base).fTokenType);
        else
            setTokenType(SPECIAL_TOKEN_IDREFNAME);
    }

    /**
     * return value of whiteSpace facet
     */
    public short getWSFacet() {
        return COLLAPSE;
    }

    /**
     * Checks that "content" string is valid
     * datatype.
     * If invalid a Datatype validation exception is thrown.
     *
     * @param content A string containing the content to be validated
     * @param derivedBylist
     *                Flag which is true when type
     *                is derived by list otherwise it
     *                it is derived by extension.
     *
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see         org.apache.xerces.validators.datatype.InvalidDatatypeValueException
     */
    public Object validate(String content, ValidationContext state ) throws InvalidDatatypeValueException{
        
        return checkContent (content, state, false);

    }


    private Object checkContent( String content, ValidationContext state, boolean asBase )
    throws InvalidDatatypeValueException {
        // validate against parent type if any
        if (fBaseValidator instanceof IDREFDatatypeValidator) {
            // validate content as a base type
            ((IDREFDatatypeValidator)fBaseValidator).checkContent(content, state, true);
        }

        // we check pattern first
        if ((fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0) {
            if (fRegex == null || fRegex.matches( content) == false)
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' does not match regular expression facet '" + fPattern + "'." );
        }


        // if this is a base validator, we only need to check pattern facet
        // all other facet were inherited by the derived type
        if (asBase)
            return content;

        // state could be null when we validate schema (facet enumeration)
        if (state != null) {
            state.addIdRef(content);
            if (!state.isIdDeclared(content)) {
                InvalidDatatypeValueException error = new InvalidDatatypeValueException( content );
            }
        }
        return content;
    }
    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


}
