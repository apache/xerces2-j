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
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;

/**
 * DataTypeValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class IDDatatypeValidator extends AbstractDatatypeValidator {
    private DatatypeValidator         fBaseValidator = null;
    private boolean                   fDerivedByList = false;
    private Object                        fNullValue = null;
    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private Hashtable                     fTableOfId;
    private Locale                 fLocale           = null;
    public static final  int          IDREF_STORE    = 0;



    public IDDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public IDDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                                 boolean derivedByList ) throws InvalidDatatypeFacetException  {
        fDerivedByList = derivedByList;
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
        //Pass content as a String
        //System.out.println("Call to ID= " );
        if (!XMLCharacterProperties.validName(content)) {//Check if is valid key-[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
            InvalidDatatypeValueException error =  new
                            InvalidDatatypeValueException( "ID is not valid: " + content );
            error.setMinorCode(XMLMessages.MSG_ID_INVALID);
            error.setMajorCode(XMLMessages.VC_ID);
            throw error;
        }
        if(!addId( content, IDStorage) ){
            InvalidDatatypeValueException error = 
                   new InvalidDatatypeValueException( "ID" + content +" has to be unique" );
            error.setMinorCode(XMLMessages.MSG_ID_NOT_UNIQUE);
            error.setMajorCode(XMLMessages.VC_ID);
            throw error;
        }
        //System.out.println("IDStorage = " + IDStorage );
        return fTableOfId;//Return the table of Id
    }

    /**
     * REVISIT
     * Compares two Datatype for order
     * 
     * @param o1
     * @param o2
     * @return 
     */
    public int compare( String content1, String content2){
        return -1;
    }

    public Hashtable getFacets(){
        return null;
    }

    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


    /**
     * Name of base type as a string.
     * A Native datatype has the string "native"  as its
     * base type.
     * 
     * @param base   the validator for this type's base type
     */
    private void setBasetype(DatatypeValidator base){
        fBaseValidator = base;
    }

    /** addId. */
    private boolean addId(String content, Object idTable) {
     
         //System.out.println( "content = >>" + content + "<<" );
        //System.out.println("state = " + state );
            if ( this.fTableOfId == null ) {
               this.fTableOfId = new Hashtable();//Gain reference to table
            } else if ( this.fTableOfId.containsKey( content ) ){ 
               //System.out.println("ID - it already has this key =" + content );

                return false;
            }
            if ( this.fNullValue == null ){
                fNullValue = new Object();
            }
            //System.out.println("Before putting content" + content );
            try {
            this.fTableOfId.put( content, fNullValue ); 
            } catch( Exception ex ){
                ex.printStackTrace();
            }
        return true;
    } // addId(int):boolean


    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }


    private String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        } catch (Exception e) {
            return "Illegal Errorcode "+minor;
        }
    }


}
