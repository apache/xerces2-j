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

/**
 * DataTypeValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class IDDatatypeValidator extends StringDatatypeValidator {

    public IDDatatypeValidator () {
        this( null, null, false, null ); // Native, No Facets defined, Restriction
    }

    public IDDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                 boolean derivedByList, XMLErrorReporter reporter) {

        // all facets are handled in StringDatatypeValidator
        super (base, facets, derivedByList, reporter);

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if (derivedByList)
            return;

        // the type is NAME by default
        // REVISIT: how to set token type for id/idref derived types
        if (base instanceof IDDatatypeValidator)
            setTokenType(((IDDatatypeValidator)base).fTokenType);
        else
            setTokenType(SPECIAL_TOKEN_IDNAME);
    }

    /**
     * return value of whiteSpace facet
     */
    public short getWSFacet() {
        return COLLAPSE;
    }

    /**
     * Make sure that ID is unique in the document
     * 
     * @param content Id value
     * @param state   a structure that stores id's
     * @return content
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     */
    public Object validate(String content, ValidationContext state ) throws InvalidDatatypeValueException{

        // REVISIT: in case user uses pattern we may not validate correctly
        //          since we don't inherit pattern for now.
        // 


        if (state != null) {
            if (state.isIdDeclared(content)) {
                throw new InvalidDatatypeValueException( "ID '" + content +"'  has to be unique" );
            }
            state.addId(content);
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
