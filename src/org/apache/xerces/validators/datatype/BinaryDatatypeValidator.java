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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.utils.Base64;
import org.apache.xerces.utils.HexBin;


/**
 *
 * binaryValidator validates that XML content is a W3C binary type.
 *
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @version $Id$
 */

public class BinaryDatatypeValidator extends AbstractDatatypeValidator {
    private DatatypeValidator  fBaseValidator   = null; //Basetype null means this is a native type
    private int                fLength          = 0;
    private int                fMaxLength       = Integer.MAX_VALUE;
    private int                fMinLength       = 0;
    private String             fPattern         = null;
    private Vector             fEnumeration     = null;
    private int                fFacetsDefined   = 0;
    private String             fEncoding        = SchemaSymbols.ATTVAL_BASE64;//default Base64 encoding
    private boolean            fDerivedByList   = false; // Default is restriction 

    public BinaryDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public BinaryDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                          boolean derivedByList ) throws InvalidDatatypeFacetException {
        if( base != null )
            setBasetype( base ); // Set base type 

        // Set Facets if any defined

        if ( facets != null  )  {
            if ( derivedByList == false ) {
                for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    if ( key.equals(SchemaSymbols.ELT_LENGTH ) ) {
                        fFacetsDefined += DatatypeValidator.FACET_LENGTH;
                        String lengthValue = (String)facets.get(key);
                        try {
                            fLength     = Integer.parseInt( lengthValue );
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDatatypeFacetException("Length value '"+
                                                           lengthValue+"' is invalid.");
                        }
                        if ( fLength < 0 )
                            throw new InvalidDatatypeFacetException("Length value '"+
                                          lengthValue+"'  must be a nonNegativeInteger.");

                    } else if (key.equals(SchemaSymbols.ELT_MINLENGTH) ) {
                        fFacetsDefined += DatatypeValidator.FACET_MINLENGTH;
                        String minLengthValue = (String)facets.get(key);
                        try {
                            fMinLength     = Integer.parseInt( minLengthValue );
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDatatypeFacetException("maxLength value '"+minLengthValue+"' is invalid.");
                        }
                    } else if (key.equals(SchemaSymbols.ELT_MAXLENGTH) ) {
                        fFacetsDefined += DatatypeValidator.FACET_MAXLENGTH;
                        String maxLengthValue = (String)facets.get(key);
                        try {
                            fMaxLength     = Integer.parseInt( maxLengthValue );
                        } catch (NumberFormatException nfe) {
                            throw new InvalidDatatypeFacetException("maxLength value '"+maxLengthValue+"' is invalid.");
                        }
                    } else if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                        fFacetsDefined += DatatypeValidator.FACET_PATTERN;
                        fPattern = (String)facets.get(key);
                    } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                        fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                        fEnumeration = (Vector)facets.get(key);
                    } else if (key.equals(SchemaSymbols.ELT_ENCODING )) {
                        fFacetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                        fEncoding = (String)facets.get(key);
                    } else {
                        throw new InvalidDatatypeFacetException();
                    }
                }
                if (((fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                    if (((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException(
                                                    "It is an error for both length and maxLength to be members of facets." );  
                    } else if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException(
                                                    "It is an error for both length and minLength to be members of facets." );
                    }
                }

                if ( ( (fFacetsDefined & ( DatatypeValidator.FACET_MINLENGTH |
                                           DatatypeValidator.FACET_MAXLENGTH) ) != 0 ) ) {
                    if ( fMinLength > fMaxLength ) {
                        throw new InvalidDatatypeFacetException( "Value of maxLength = " + fMaxLength +
                                                      "must be greater that the value of minLength" + fMinLength );
                    }
                }
            } else {  //Derivation by List 
                fDerivedByList = true;
            }
        }// End of Facet setting
    }



    /**
     * validate that a string is a W3C binary type
     *
     * validate returns true or false depending on whether the string content is an
     * instance of the W3C binary datatype
     *
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  not a W3C binary type
     */
    public Object validate(String content, Object state ) throws InvalidDatatypeValueException {
        if( fFacetsDefined == 0 )
           {
           throw new InvalidDatatypeValueException( "Constrain encoding required for binary datatype" );
           }

        if ( fDerivedByList == false) { //derived by restriction
            if (((fFacetsDefined & DatatypeValidator.FACET_ENCODING) != 0 ) ){ //Encode defined then validate
                if ( fEncoding.equals( SchemaSymbols.ATTVAL_BASE64)){ //Base64
                    if ( Base64.isBase64( content ) == false ) {
                        throw new InvalidDatatypeValueException( "Value '"+
                                                                 content+ "'  must be" + "is not encoded in Base64" );
                    }
                } else { //HexBin
                    if ( HexBin.isHex( content ) == false ){
                        throw new InvalidDatatypeValueException( "Value '"+
                                                                 content+ "'  must be" + "is not encoded in Hex" );
                    }
                }
            }
        } else{ //derived by list
        }
        return null;
    }

    /**
     * Compare two Binary Datatype Lexical values.
     * 
     * @param content1
     * @param content2
     * @return 
     */
    public int compare( String content1, String content2){
        return 0;
    }

    /**
     * Returns a Hastable that represent the facets
     * state of the datatype.
     * 
     * @return 
     */
    public Hashtable getFacets(){
        return null;
    }

    
     /**
     * Returns a copy of this object.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }

    //Private methods

    /**
     * Set base type
     * 
     * @param base
     */
    private void setBasetype(DatatypeValidator base) {
        fBaseValidator = base;
    }



}
