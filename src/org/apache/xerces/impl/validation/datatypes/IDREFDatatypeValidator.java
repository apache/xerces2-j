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
import org.apache.xerces.impl.validation.DatatypeValidator;
import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;
import org.apache.xerces.util.XMLChar;
import java.util.NoSuchElementException;
//import org.apache.xerces.utils.XMLMessages;




/**
 * IDREFValidator defines the interface that data type validators must obey.
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 * 
 * @author Jeffrey Rodriguez-
 * @version $Id$
 */
public class IDREFDatatypeValidator extends AbstractDatatypeValidator {
    private DatatypeValidator fBaseValidator    = null;
    private boolean           fDerivedByList    = false;
    private Hashtable              fTableOfId   = null; //This is pass to us through the state object
    private Hashtable              fTableIDRefs = null;
    private Object                   fNullValue = null;
    private Locale            fLocale           = null;
    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();

    public static final  int       IDREF_STORE    = 0;
    public static final  int       IDREF_CLEAR    = 1;
    public static final  int       IDREF_VALIDATE = 2; 



    public IDREFDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public IDREFDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                                    boolean derivedByList ) throws InvalidDatatypeFacetException { 

        fDerivedByList = derivedByList;
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
        //Pass content as a String
        //System.out.println( "base = " + this.fBaseValidator );

        //if( this.fBaseValidator != null ){
          //  this.fBaseValidator.validate( content, state );
        //}
        StateMessageDatatype message;
        if ( this.fDerivedByList == false ){
            //System.out.println("conten = " + content );
            if (state!= null){
                message = (StateMessageDatatype) state;    
                if (message.getDatatypeState() == IDREFDatatypeValidator.IDREF_CLEAR ){
                    if ( this.fTableOfId != null ){
                        fTableOfId.clear(); //This is pass to us through the state object
                    }
                    if ( this.fTableIDRefs != null ){
                        fTableIDRefs.clear(); 
                    }
                    return null;
                } else if ( message.getDatatypeState() == IDREFDatatypeValidator.IDREF_VALIDATE ){
                    this.checkIdRefs();//Validate that all keyRef is a keyIds
                } else if ( message.getDatatypeState() == IDREFDatatypeValidator.IDREF_STORE ) {
                    this.fTableOfId = (Hashtable) message.getDatatypeObject();
                    if (!XMLCharacterProperties.validName(content)) {//Check if is valid key

                        InvalidDatatypeValueException error = new InvalidDatatypeValueException( "IDREF is not valid" );//Need Message

                        error.setMinorCode(XMLMessages.MSG_IDREF_INVALID );
                        error.setMajorCode(XMLMessages.VC_IDREF);
                        throw error;//Need Message
                    }
                    //System.out.println("Content REF = " + content );
                    addIdRef( content, state);// We are storing IDs 
                }
            }
         } else {
            //System.out.println("list = " + content );
            if (state!= null){
                message = (StateMessageDatatype) state;    
                if (message.getDatatypeState() == IDREFDatatypeValidator.IDREF_CLEAR ){
                    if ( this.fTableOfId != null ){
                        fTableOfId.clear(); //This is pass to us through the state object
                    }
                    if ( this.fTableIDRefs != null ){
                        fTableIDRefs.clear(); 
                    }
                    return null;

                } else if ( message.getDatatypeState() == IDREFDatatypeValidator.IDREF_VALIDATE ){
                    //System.out.println("Call to Validate IDREFS" );
                    this.checkIdRefs();//Validate that all keyRef is a keyIds
                } else if ( message.getDatatypeState() == IDREFDatatypeValidator.IDREF_STORE ) {
                    //System.out.println("IDREFS = " + content );
                    StringTokenizer   tokenizer = new StringTokenizer( content );
                    this.fTableOfId = (Hashtable) message.getDatatypeObject();
                    while ( tokenizer.hasMoreTokens() ) {
                        String idName = tokenizer.nextToken(); 
                        //System.out.println("idName here = " + idName );
                        if( this.fBaseValidator != null ){
                               this.fBaseValidator.validate( idName, state );
                        }
                        addIdRef( idName, state);// We are storing IDs 
                    }
                }

            }

        }
        return null;
        */
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
    private void addIdRef(String content, Object state) {
        //System.out.println("this.fTableOfId = " + content );
        //System.out.println("state =  " + state  );
        //System.out.println("table = " + this.fTableOfId );


        if ( this.fTableOfId != null &&  this.fTableOfId.containsKey( content ) ){
            //System.out.println("It already contains key = " + content );
            return;
        }
        //System.out.println("Table of IDRefs = " + this.fTableIDRefs );
        if ( this.fTableIDRefs == null ){
            this.fTableIDRefs = new Hashtable();
        } else if ( fTableIDRefs.containsKey( content ) ){
            return;
        }


        if ( this.fNullValue == null ){
            fNullValue = new Object();
        }
        //System.out.println("tabl IDREFs = " + this.fTableIDRefs );
        try {
            this.fTableIDRefs.put( content, fNullValue ); 
        } catch( OutOfMemoryError ex ){
            System.out.println( "Out of Memory: Hashtable of ID's has " + this.fTableIDRefs.size() + " Elements" );
            ex.printStackTrace();
        }
    } // addId(int):boolean


    private void checkIdRefs() throws InvalidDatatypeValueException {

        if ( this.fTableIDRefs == null)
            return;
        
        Enumeration en = this.fTableIDRefs.keys();
        //System.out.println("TabIDREFs=  " + this.fTableIDRefs );

        while (en.hasMoreElements()) {
            String key = (String)en.nextElement();
            //System.out.println( "Key here = x>>" + key + "<<" );
            //System.out.println("Tab Ids = " + this.fTableOfId );
            if ( this.fTableOfId == null || ! this.fTableOfId.containsKey(key)) {
                
                InvalidDatatypeValueException error =  new
                            InvalidDatatypeValueException( key );
                /* TODO in Xerces 2
                error.setMinorCode(XMLMessages.MSG_ELEMENT_WITH_ID_REQUIRED);
                error.setMajorCode(XMLMessages.VC_IDREF);
                */
                throw error;
            }
        }

    } // checkIdRefs()


    /**
    * set the locate to be used for error messages
    */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }


    private String getErrorString(int major, int minor, Object args[]) {
        //return fMessageProvider.createMessage(fLocale, major, minor, args);
        return fMessageProvider.formatMessage( fLocale, null, null);
    }


}

