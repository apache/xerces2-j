/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights 
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
package org.apache.xerces.impl.dv.dtd;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.StringTokenizer;
import org.apache.xerces.util.XMLChar;
import java.util.NoSuchElementException;


/**
 * <P>IDDatatypeValidator - ID represents the ID attribute
 * type from XML 1.0 Recommendation. The value space
 * od ID is the set of all strings that match the
 * NCName production and have been used in an XML
 * document. The lexical space of ID is the set of all
 * strings that match the NCName production.</P>
 * <P>The value space of ID is scoped to a specific
 * instance document.</P>
 * <P>The following constraint applies:
 * An ID must not appear more than once in an XML
 * document as a value of this type; i.e., ID values
 * must uniquely identify the elements which bear
 * them.</P>
 * <P>An ID validator is a statefull validator, it needs
 * read/write access to the associated instant document
 * table of IDs.</P>
 * <P>
 * The following snippet shows typical use of the
 * the IDDatatype:</P>
 * <CODE>
 * <PRE>
 *  DatatypeValidator  idData = tstRegistry.getDatatypeValidator( "ID" );
 * 
 *       if (  idData != null ) {
 *          ((IDDatatypeValidator) idData).initialize();
 *          try {
 *             idData.validate( "a1", null );
 *             idData.validate( "a2", null );
 *          } catch ( Exception ex ) {
 *             ex.printStackTrace();
 *          }
 *          Hashtable tst = (Hashtable)((IDDatatypeValidator) idData).getTableIds();
 *          if (tst != null) {
 *             System.out.println("Table of ID = " + tst.toString());
 *          }
 * 
 *       }
 * 
 *       DatatypeValidator idRefData = tstRegistry.getDatatypeValidator("IDREF" );
 *       if( idRefData != null ){
 *          IDREFDatatypeValidator refData = (IDREFDatatypeValidator) idRefData;
 *          refData.initialize( ((IDDatatypeValidator) idData).getTableIds());
 *          try {
 *             refData.validate( "a1", null );
 *             refData.validate( "a2", null );
 *             //refData.validate( "a3", null );//Should throw exception at validate()
 *             refData.validate();
 *          } catch( Exception ex ){
 *             ex.printStackTrace();
 *          }
 *       }
 *       </PRE>
 * </CODE>
 * 
 * @author Jeffrey Rodriguez
 * @version $Id$
 * @see AbstractDatatypeValidator
 * @see DatatypeValidator
 * @see IDREFDatatypeValidator
 */
public class IDDatatypeValidator extends AbstractDatatypeValidator 
implements StatefullDatatypeValidator {
    private DatatypeValidator       fBaseValidator;
    private Object                  fNullValue;
    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private Hashtable               fTableOfId;
    private Locale                  fLocale;



    public IDDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public IDDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                                 boolean derivedByList ) throws InvalidDatatypeFacetException  {
    }



    /**
     * Checks that "content" string is valid
     * datatype.
     * If invalid a Datatype validation exception is thrown.
     * 
     * @param content A string containing the content to be validated
     * @param state  Generic Object state that can be use to pass
     *               Structures
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see InvalidDatatypeValueException
     */
    public void validate(String content, Object state ) throws InvalidDatatypeValueException{

        boolean status;
        if ((status = XMLChar.isValidName(content) ) == false) {//Check if is valid key-[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
            InvalidDatatypeValueException error =  new
                                                   InvalidDatatypeValueException( content );
            error.setKeyIntoReporter( "IDInvalid" );
            throw error;
        }
        if (!addId( content)) { //It is OK to pass a null here
            InvalidDatatypeValueException error = 
            new InvalidDatatypeValueException( content );
            error.setKeyIntoReporter( "IDNotUnique" );
            throw error;
        }
    }

    /**
     * A no-op method in this Datatype
     */
    public void validate() throws InvalidDatatypeValueException{
    }


    /**
     * Initializes internal table of IDs used
     * by ID datatype validator to keep track
     * of ID's.
     * This method is unique to IDDatatypeValidator.
     * 
     * @param state  This is a dummy parameter.
     */
    public void initialize( Object tableOfIDs) {
        fTableOfId = (Hashtable) tableOfIDs;
    }

    /**
     * REVISIT
     * Compares two Datatype for order
     * 
     * @param o1
     * @param o2
     * @return 0 if value1 and value2 are equal, a value less than 0 if value1 is less than value2, a value greater than 0 if value1 is greater than value2
     */
    public int compare( String content1, String content2) {
        return -1;
    }

    public Hashtable getFacets() {
        return null;
    }

    /**
       * Return a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }

    /**
     * This method is unique to IDDatatypeValidator.
     * It returns a reference to the internal ID table.
     * This method should be used by the IDREF datatype
     * validator which needs read access to ID table.
     * 
     * @return internal state of the validator (in this case the ID table)
     */
    public Object getInternalStateInformation() {
        return fTableOfId;
    }


    /**
     * Name of base type as a string.
     * A Native datatype has the string "native"  as its
     * base type.
     * 
     * @param base   the validator for this type's base type
     */
    private void setBasetype(DatatypeValidator base) {
        fBaseValidator = base;
    }

    /**
     * Adds validated ID to internal table of ID's.
     * We check ID uniqueness constraint.
     * 
     * @param content
     * @return    If ID validated is not unique we return a false and
     *         then validate method throws a validation exception.
     */
    private boolean addId(String content) {
        if (fTableOfId.containsKey( content ) == true ) {
            return false;
        }
        if (this.fNullValue == null) {
            fNullValue = new Object();
        }
        try {
            fTableOfId.put( content, fNullValue ); 
        } catch (Exception ex) {
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
        //return fMessageProvider.createMessage(fLocale, major, minor, args);
        return fMessageProvider.formatMessage(fLocale, null, null );
    }


}
