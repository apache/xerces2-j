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
import java.util.Enumeration;
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.validators.schema.SchemaSymbols;

/**
 * DataTypeValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class IDDatatypeValidator extends StringDatatypeValidator {
    private static StringDatatypeValidator  fgStrValidator  = null;
    private static Object                   fNullValue      = null;
    protected static Hashtable              fTableOfId      = null;

    public static final  int                IDREF_STORE     = 0;
    public static final  int                ID_CLEAR        = 1;


    public IDDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public IDDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                 boolean derivedByList ) throws InvalidDatatypeFacetException  {

        // all facets are handled in StringDatatypeValidator
        super (base, facets, derivedByList);

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        // make a string validator for NCName
        if ( fgStrValidator == null) {
            Hashtable strFacets = new Hashtable();
            strFacets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATT_COLLAPSE);
            strFacets.put(SchemaSymbols.ELT_PATTERN , "[\\i-[:]][\\c-[:]]*"  );
            fgStrValidator = new StringDatatypeValidator (null, strFacets, false);
        }
    }

    /**
     * return value of whiteSpace facet
     */
    public short getWSFacet(){
        return fgStrValidator.getWSFacet();
    }

    /**
     * Checks that "content" string is valid
     * datatype.
     * If invalid a Datatype validation exception is thrown.
     *
     * @param content A string containing the content to be validated
     * @param state  Generic Object state that can be use to pass
     *               Structures
     * @return
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see org.apache.xerces.validators.datatype.InvalidDatatypeValueException
     */
    public Object validate(String content, Object IDStorage ) throws InvalidDatatypeValueException{

        // no validation if asked to clear ID hash table
        if (IDStorage != null) {
            StateMessageDatatype message = (StateMessageDatatype) IDStorage;
            if (message.getDatatypeState() == IDDatatypeValidator.ID_CLEAR ){
                if ( this.fTableOfId != null ){
                    this.fTableOfId.clear();
                    this.fTableOfId = null;
                }
                return null;
            }
        }

        // use StringDatatypeValidator to validate content against facets
        super.validate(content, IDStorage);

        // check if content is a valid NCName
        try {
            fgStrValidator.validate(content, null);
        } catch (InvalidDatatypeValueException idve) {
            InvalidDatatypeValueException error =  new InvalidDatatypeValueException( "ID is not valid: " + content );
            error.setMinorCode(XMLMessages.MSG_ID_INVALID);
            error.setMajorCode(XMLMessages.VC_ID);
            throw error;
        }

        //Check if is valid key-[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
        /*if (!XMLCharacterProperties.validName(content)) {
            InvalidDatatypeValueException error =  new
                                                    InvalidDatatypeValueException( "ID is not valid: " + content );
            error.setMinorCode(XMLMessages.MSG_ID_INVALID);
            error.setMajorCode(XMLMessages.VC_ID);
            throw error;
        }*/

        if (!addId( content, IDStorage) ) {
            InvalidDatatypeValueException error =
            new InvalidDatatypeValueException( "ID '" + content +"'  has to be unique" );
            error.setMinorCode(XMLMessages.MSG_ID_NOT_UNIQUE);
            error.setMajorCode(XMLMessages.VC_ID);
            throw error;
        }

        //Return the table of Ids
        return fTableOfId;
    }


    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


    /** addId. */
    private boolean addId(String content, Object idTable) {

        if ( this.fTableOfId == null ) {
            this.fTableOfId = new Hashtable();
        } else if ( this.fTableOfId.containsKey( content ) ){
            return false;
        }
        if ( this.fNullValue == null ){
            fNullValue = new Object();
        }
        try {
            this.fTableOfId.put( content, fNullValue );
        } catch( OutOfMemoryError ex ){
            System.out.println( "Out of Memory: Hashtable of ID's has " + this.fTableOfId.size() + " Elements" );
            ex.printStackTrace();
        }
        return true;
    } // addId(int):boolean
}
