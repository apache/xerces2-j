/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

/**
 * QName Validator validates a QName type.
 * QName represents XML qualified names. The value
 * space of QName is the set of tuples
 * {namespace name, local part}, where namespace
 * name is a anyURI and local part is an NCName.
 * The lexical space of QName is the set of strings
 * that match the QName production of [Namespaces in
 * XML].
 *
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class QNameDatatypeValidator extends  AbstractDatatypeValidator {
    
    private Locale    fLocale          = null;
    private int       fLength          = 0;
    private int       fMaxLength       = Integer.MAX_VALUE;
    private int       fMinLength       = 0;
    private Vector    fEnumeration     = null;

    // for the NCName validator
    private static StringDatatypeValidator  fgStrValidator  = null;

    public QNameDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public QNameDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                    boolean derivedByList ) throws InvalidDatatypeFacetException  {

        // make a string validator for NCName
        if ( fgStrValidator == null) {
            Hashtable strFacets = new Hashtable();
            strFacets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATT_COLLAPSE);
            strFacets.put(SchemaSymbols.ELT_PATTERN , "[\\i-[:]][\\c-[:]]*"  );
            fgStrValidator = new StringDatatypeValidator (null, strFacets, false);
        }

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
                QNameDatatypeValidator qNameBase = (QNameDatatypeValidator)base;

                // check 4.3.1.c1 error: length & (base.maxLength | base.minLength)
                if (((fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                    if (((qNameBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and maxLength to be members of facets." );
                    } else if (((qNameBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and minLength to be members of facets." );
                    }
                }

                // check 4.3.1.c1 error: base.length & (maxLength | minLength)
                if (((qNameBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                    if (((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and maxLength to be members of facets." );
                    } else if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                        throw new InvalidDatatypeFacetException("It is an error for both length and minLength to be members of facets." );
                    }
                }

                // check 4.3.2.c1 must: minLength <= base.maxLength
                if (((fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) &&
                    ((qNameBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    if ( fMinLength > qNameBase.fMaxLength ) {
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                    }
                }

                // check 4.3.2.c1 must: base.minLength <= maxLength
                if (((qNameBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) &&
                    ((fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    if ( qNameBase.fMinLength > fMaxLength ) {
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                    }
                }

                // check 4.3.1.c2 error: length != base.length
                if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 &&
                     (qNameBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
                    if ( fLength != qNameBase.fLength )
                        throw new InvalidDatatypeFacetException( "Value of length = '" + fLength +
                                                                 "' must be = the value of base.length = '" + qNameBase.fLength + "'.");
                }
                // check 4.3.2.c2 error: minLength < base.minLength
                if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 &&
                     (qNameBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
                    if ( fMinLength < qNameBase.fMinLength )
                        throw new InvalidDatatypeFacetException( "Value of minLength = '" + fMinLength +
                                                                 "' must be >= the value of base.minLength = '" + qNameBase.fMinLength + "'.");
                }
                // check 4.3.3.c1 error: maxLength > base.maxLength
                if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 &&
                     (qNameBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
                    if ( fMaxLength > qNameBase.fMaxLength )
                        throw new InvalidDatatypeFacetException( "Value of maxLength = '" + fMaxLength +
                                                                 "' must be <= the value of base.maxLength = '" + qNameBase.fMaxLength + "'.");
                }

                // inherit length
                if ( (qNameBase.fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_LENGTH;
                        fLength = qNameBase.fLength;
                    }
                }
                // inherit minLength
                if ( (qNameBase.fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_MINLENGTH;
                        fMinLength = qNameBase.fMinLength;
                    }
                }
                // inherit maxLength
                if ( (qNameBase.fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
                    if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) == 0 ) {
                        fFacetsDefined |= DatatypeValidator.FACET_MAXLENGTH;
                        fMaxLength = qNameBase.fMaxLength;
                    }
                }
                // inherit enumeration
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) == 0 &&
                     (qNameBase.fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                    fEnumeration = qNameBase.fEnumeration;
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
    public Object validate(String content, Object state)  throws InvalidDatatypeValueException
    {
        checkContent( content, false );
        return null;
    }

    /**
    * set the locate to be used for error messages
    */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    private void checkContent( String content, boolean asBase )throws InvalidDatatypeValueException
    {
        // validate against parent type if any
        if ( this.fBaseValidator != null ) {
            // validate content as a base type
                ((QNameDatatypeValidator)fBaseValidator).checkContent(content, true);
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

        if ( (fFacetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
            if ( content.length() > fMaxLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+content.length()+
                                                        "' exceeds maximum length facet of '"+fMaxLength+"'.");
            }
        }
        if ( (fFacetsDefined & DatatypeValidator.FACET_MINLENGTH) != 0 ) {
            if ( content.length() < fMinLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+content.length()+
                                                        "' is less than minimum length facet of '"+fMinLength+"'." );
            }
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_LENGTH) != 0 ) {
            if ( content.length() != fLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+content.length()+
                                                        "' is not equal to length facet '"+fLength+"'.");
            }
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 &&
             (fEnumeration != null) ) {
            if ( fEnumeration.contains( content ) == false )
                throw new InvalidDatatypeValueException("Value '"+content+"' must be one of "+fEnumeration);
        }

        // check 3.2.18.c0 must: "NCName:NCName"
        try {
            int posColon = content.indexOf(':');
            if (posColon >= 0)
                fgStrValidator.validate(content.substring(0,posColon), null);
            fgStrValidator.validate(content.substring(posColon+1), null);
        } catch (InvalidDatatypeValueException idve) {
            throw new InvalidDatatypeValueException("Value '"+content+"' is not a valid QName");
        }
    }

    public int compare( String content, String facetValue ){
        Locale    loc       = Locale.getDefault();
        Collator  collator  = Collator.getInstance( loc );
        return collator.compare( content, facetValue );
    }

    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }
}
