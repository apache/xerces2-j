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

package org.apache.xerces.impl.validation.datatypes;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.xerces.util.SymbolTable;

import org.apache.xerces.impl.validation.DatatypeValidator;

//import org.apache.xerces.validators.datatype.StateMessageDatatype;
import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;


/**
 * ENTITYDatatypeValidator implements the
 * DatattypeValidator interface.
 * This validator embodies the ENTITY attribute type
 * from XML1.0 recommendation.
 * The Value space of ENTITY is the set of all strings
 * that match the NCName production and have been
 * declared as an unparsed entity in a document
 * type definition.
 * The Lexical space of Entity is the set of all
 * strings that match the NCName production.
 * The value space of ENTITY is scoped to a specific
 * instance document.
 * 
 * Some caveats:
 * 
 * Because of the Xerces Architecture, where all
 * symbols are stored in a SymbolTable and Strings
 * are referenced by int then this datatype needs
 * to know about SymbolTable.
 * The first time that this datatype is invoked
 * we pass a message containing 2 references needed
 * by this validator:
 * - a reference to the DefaultEntityHandler  used
 * by the XMLValidator.
 * - a reference to the SymbolTable.
 * 
 * 
 * This validator extends also the XML1.0 validation
 * provided in DTD by providing "only on Schemas"
 * facet validation.
 * This validator also embodies the Derived datatype
 * ENTITIES which is an ENTITY derived by list.
 * 
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * 
 * @author Jeffrey Rodriguez-
 * @version $Id$
 * @see org.apache.xerces.validators.datatype.DatatypeValidator
 * @see org.apache.xerces.validators.datatype.DatatypeValidatorFactoryImpl
 * @see org.apache.xerces.validators.datatype.DatatypeValidatorFactory
 * @see org.apache.xerces.validators.common.XMLValidator
 */
public class ENTITYDatatypeValidator extends AbstractDatatypeValidator {
    private DatatypeValidator        fBaseValidator    = null;
    //private DefaultEntityHandler     fEntityHandler    = null;
    private SymbolTable              fSymbolTable       = null;

    public  static final int         ENTITY_INITIALIZE = 0;


    public ENTITYDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public ENTITYDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                     boolean derivedByList  ) throws InvalidDatatypeFacetException {

        setBasetype( base ); // Set base type
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
    public void validate(String content, Object state ) throws InvalidDatatypeValueException{
        /*

        StateMessageDatatype message = (StateMessageDatatype) state;
        int                  attValueHandle;


        if ( message!= null && message.getDatatypeState() == ENTITYDatatypeValidator.ENTITY_INITIALIZE ){
            Object[]   unpackMessage = (Object[] ) message.getDatatypeObject();


            this.fEntityHandler      = (DefaultEntityHandler) unpackMessage[0];
            this.SymbolTable        = (SymbolTable) unpackMessage[1];
        } else {


            if ( this.fEntityHandler == null ) {
                InvalidDatatypeValueException error = 
                new InvalidDatatypeValueException( "ERROR: ENTITYDatatype Validator: Failed Initialization DefaultEntityHandler is null" );//Need Message
                throw error;
            }
            if ( this.fStringPool == null ) {
                InvalidDatatypeValueException error = 
                new InvalidDatatypeValueException( "ERROR: ENTITYDatatype Validator: Failed Initialization StrinPool is null" );//Need Message
                throw error;
            }



                attValueHandle = this.SymbolTable.addSymbol( content );
                if (!this.fEntityHandler.isUnparsedEntity( attValueHandle ) ) {
                    InvalidDatatypeValueException error = 
                    new InvalidDatatypeValueException( "ENTITY '"+ content +"' is not valid" );//Need Message
                    error.setMinorCode(XMLMessages.MSG_ENTITY_INVALID );
                    error.setMajorCode(XMLMessages.VC_ENTITY_NAME);
                    throw error;
                }
           
        }
        */
    }

    /**
     * REVISIT
     * Compares two Datatype for order
     * 
     * @return 
     */
    public int compare( String  content1, String content2){
        return -1;
    }

    public Hashtable getFacets(){
        return null;
    }

    // Private methods start here

    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


    /**
     * 
     * @param base   the validator for this type's base type
     */
    private void setBasetype(DatatypeValidator base){
        fBaseValidator = base;
    }



}
