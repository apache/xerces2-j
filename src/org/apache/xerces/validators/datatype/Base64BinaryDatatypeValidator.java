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
import java.util.Vector;
import java.util.Enumeration;
import java.util.Locale;
import java.text.Collator;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.utils.Base64;

/**
 * Base64BinaryValidator validates that XML content is a W3C string type.
 * @author Ted Leung
 * @author Kito D. Mann, Virtua Communications Corp.
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class Base64BinaryDatatypeValidator extends AbstractDatatypeValidator{
    private Locale     fLocale          = null;
    private int        fLength          = 0;
    private int        fMaxLength       = Integer.MAX_VALUE;
    private int        fMinLength       = 0;
    private Vector     fEnumeration     = null;


    public  Base64BinaryDatatypeValidator () throws InvalidDatatypeFacetException{
        this( null, null, false ); // Native, No Facets defined, Restriction

    }

    public Base64BinaryDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                           boolean derivedByList ) throws InvalidDatatypeFacetException {

         // Set base type
        fBaseValidator = base;

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        // Set Facets if any defined
        if ( facets != null  ){
            for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();

                if ( key.equals(SchemaSymbols.ELT_LENGTH) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_LENGTH;
                    String lengthValue = (String)facets.get(key);
                    try {
                        fLength     = Integer.parseInt( lengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new InvalidDatatypeFacetException("Length value '"+lengthValue+"' is invalid.");
                    }
                    // check 4.3.1.c0 must: length >= 0
                    if ( fLength < 0 )
                        throw new InvalidDatatypeFacetException("Length value '"+lengthValue+"'  must be a nonNegativeInteger.");

                } else if (key.equals(SchemaSymbols.ELT_MINLENGTH) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MINLENGTH;
                    String minLengthValue = (String)facets.get(key);
                    try {
                        fMinLength     = Integer.parseInt( minLengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new InvalidDatatypeFacetException("minLength value '"+minLengthValue+"' is invalid.");
                    }
                    // check 4.3.2.c0 must: minLength >= 0
                    if ( fMinLength < 0 )
                        throw new InvalidDatatypeFacetException("minLength value '"+minLengthValue+"'  must be a nonNegativeInteger.");

                } else if (key.equals(SchemaSymbols.ELT_MAXLENGTH) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MAXLENGTH;
                    String maxLengthValue = (String)facets.get(key);
                    try {
                        fMaxLength     = Integer.parseInt( maxLengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new InvalidDatatypeFacetException("maxLength value '"+maxLengthValue+"' is invalid.");
                    }
                    // check 4.3.3.c0 must: maxLength >= 0
                    if ( fMaxLength < 0 )
                        throw new InvalidDatatypeFacetException("maxLength value '"+maxLengthValue+"'  must be a nonNegativeInteger.");


                } else if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                    fFacetsDefined |= DatatypeValidator.FACET_PATTERN;
                    fPattern = (String)facets.get(key);
                    if( fPattern != null )
                        fRegex = new RegularExpression(fPattern, "X");
                } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                    fEnumeration = (Vector)facets.get(key);
                    fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                } else {
                    throw new InvalidDatatypeFacetException("invalid facet tag : " + key);
                }
            }

            if ( base != null ) {
                // check 4.3.5.c0 must: enumeration values from the value space of base
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 &&
                     (fEnumeration != null) ) {
                    int i = 0;
                    try {
                        for ( ; i < fEnumeration.size(); i++) {
                            base.validate ((String)fEnumeration.elementAt(i), null);
                        }
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of enumeration = '" + fEnumeration.elementAt(i) +
                                                                 "' must be from the value space of base.");
                    }
                }
            }

            // check 4.3.1.c1 error: length & (maxLength | minLength)
            if (((fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                if (((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    throw new InvalidDatatypeFacetException("It is an error for both length and maxLength to be members of facets." );
                } else if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                    throw new InvalidDatatypeFacetException("It is an error for both length and minLength to be members of facets." );
                }
            }

            // check 4.3.2.c1 must: minLength <= maxLength
            if ( ( (fFacetsDefined & ( DatatypeValidator.FACET_MINLENGTH |
                                        DatatypeValidator.FACET_MAXLENGTH) ) != 0 ) ) {
                if ( fMinLength > fMaxLength ) {
                    throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                             "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                }
            }

            // if base type is string, check facets against base.facets, and inherit facets from base
            if (base != null) {
                Base64BinaryDatatypeValidator base64BBase = (Base64BinaryDatatypeValidator)base;

                // check 4.3.1.c1 error: length & (base.maxLength | base.minLength)
                if (((fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                    if (((base64BBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and maxLength to be members of facets." );
                    } else if (((base64BBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and minLength to be members of facets." );
                    }
                }

                // check 4.3.1.c1 error: base.length & (maxLength | minLength)
                if (((base64BBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                    if (((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and maxLength to be members of facets." );
                    } else if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and minLength to be members of facets." );
                    }
                }

                // check 4.3.2.c1 must: minLength <= base.maxLength
                if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) &&
                    ((base64BBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    if ( fMinLength > base64BBase.fMaxLength ) {
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                    }
                }

                // check 4.3.2.c1 must: base.minLength <= maxLength
                if (((base64BBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) &&
                    ((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    if ( base64BBase.fMinLength > fMaxLength ) {
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                    }
                }

                // check 4.3.1.c2 error: length != base.length
                if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 &&
                     (base64BBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
                    if ( fLength != base64BBase.fLength )
                        throw new InvalidDatatypeFacetException( "Value of length = '" + fLength +
                                                                 "' must be = the value of base.length = '" + base64BBase.fLength + "'.");
                }
                // check 4.3.2.c2 error: minLength < base.minLength
                if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 &&
                     (base64BBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
                    if ( fMinLength < base64BBase.fMinLength )
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "' must be >= the value of base.minLength = '" + base64BBase.fMinLength + "'.");
                }
                // check 4.3.3.c1 error: maxLength > base.maxLength
                if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 &&
                     (base64BBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
                    if ( fMaxLength > base64BBase.fMaxLength )
                        throw new InvalidDatatypeFacetException( "Value of maxLength = '" + fMaxLength +
                                                                 "' must be <= the value of base.maxLength = '" + base64BBase.fMaxLength + "'.");
                }

                // inherit length
                if ( (base64BBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_LENGTH;
                        fLength = base64BBase.fLength;
                    }
                }
                // inherit minLength
                if ( (base64BBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_MINLENGTH;
                        fMinLength = base64BBase.fMinLength;
                    }
                }
                // inherit maxLength
                if ( (base64BBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_MAXLENGTH;
                        fMaxLength = base64BBase.fMaxLength;
                    }
                }
                // inherit enumeration
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) == 0 &&
                     (base64BBase.fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                    fEnumeration = base64BBase.fEnumeration;
                }
            }
        }// End of Facets Setting
    }

    /**
     * validate that a string is a W3C string type
     *
     * @param content A string containing the content to be validated
     * @param list
     * @exception throws InvalidDatatypeException if the content is
     *                   not a W3C string type
     * @exception InvalidDatatypeValueException
     */
    public Object validate(String content, Object state)  throws InvalidDatatypeValueException {
        checkContent( content, state, false );
        return null;
    }


    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }


    private void checkContent( String content, Object state, boolean asBase )
    throws InvalidDatatypeValueException {
        // validate against parent type if any
        if ( this.fBaseValidator != null ) {
            // validate content as a base type
                ((Base64BinaryDatatypeValidator)fBaseValidator).checkContent(content, state, true);
        }

        // we check pattern first
        if ( (fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
            if ( fRegex == null || fRegex.matches( content) == false )
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' does not match regular expression facet '" + fPattern + "'." );
        }

        // if this is a base validator, we only need to check pattern facet
        // all other facet were inherited by the derived type
        if (asBase)
            return;

        int base64Len = Base64.getDataLength(content.getBytes());
        if (base64Len <= 0) {
            throw new InvalidDatatypeValueException( "Value '"+content+"' is not encoded in Base64" );
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
            if ( base64Len > fMaxLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+base64Len+
                                                        "' exceeds maximum length facet of '"+fMaxLength+"'.");
            }
        }
        if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
            if ( base64Len < fMinLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+base64Len+
                                                        "' is less than minimum length facet of '"+fMinLength+"'." );
            }
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
            if ( base64Len != fLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+base64Len+
                                                        "' is not equal to length facet '"+fLength+"'.");
            }
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 &&
             (fEnumeration != null) ) {
            if ( fEnumeration.contains( content ) == false )
                throw new InvalidDatatypeValueException("Value '"+content+"' must be one of "+fEnumeration);
        }
    }


    public int compare( String value1, String value2 ){
        return 0;
    }

    /**
   * Returns a copy of this object.
   */
    public Object clone() throws CloneNotSupportedException  {
        Base64BinaryDatatypeValidator newObj = null;
        try {
            newObj = new Base64BinaryDatatypeValidator();

            newObj.fLocale           =  this.fLocale;
            newObj.fBaseValidator    =  this.fBaseValidator;
            newObj.fLength           =  this.fLength;
            newObj.fMaxLength        =  this.fMaxLength;
            newObj.fMinLength        =  this.fMinLength;
            newObj.fPattern          =  this.fPattern;
            newObj.fRegex            =  this.fRegex;
            newObj.fEnumeration      =  this.fEnumeration;
            newObj.fFacetsDefined    =  this.fFacetsDefined;
        } catch ( InvalidDatatypeFacetException ex) {
            ex.printStackTrace();
        }
        return newObj;
    }


}

