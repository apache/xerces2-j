/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;

/**
 * StringValidator validates that XML content is a W3C string type.
 * @author Ted Leung
 * @author Kito D. Mann, Virtua Communications Corp.
 * @author Jeffrey Rodriguez
 */
public class StringValidator implements DatatypeValidator {
    private Locale    fLocale          = null;
    private String    fBaseValidator   = "native";

    private int       _length          = 0;
    private int       _maxLength       = 0;
    private int       _minLength       = 0;
    private String    _pattern         = null;
    private Vector    _enumeration     = null;
    private String    _maxInclusive    = null;
    private String    _maxExclusive    = null;
    private String    _minInclusive    = null;
    private String    _minExclusive    = null;
    private int       _facetsDefined   = 0;
    private int       _derivedBy       = DatatypeValidator.DERIVED_BY_RESTRICTION;//default

    private boolean isMaxExclusiveDefined = false;
    private boolean isMaxInclusiveDefined = false;
    private boolean isMinExclusiveDefined = false;
    private boolean isMinInclusiveDefined = false;

    /**
     * validate that a string is a W3C string type
     * 
     * @param content A string containing the content to be validated
     * @param list
     * @exception throws InvalidDatatypeException if the content is
     *                   not a W3C string type
     * @exception InvalidDatatypeValueException
     */
    public void validate(String content)  throws InvalidDatatypeValueException
    {
        if ( _facetsDefined == 0 )// No Facets to validate against
            return;

        StringTokenizer parsedList = null;

        if( _derivedBy == DatatypeValidator.DERIVED_BY_RESTRICTION  ){ 
            parsedList = new StringTokenizer( content );
            try {
                while ( parsedList.hasMoreTokens() ) {
                    checkContentList( parsedList.nextToken() );
                }
            } catch ( NoSuchElementException e ) {
                e.printStackTrace();
            }
        } else {
            checkContent( content );
        }
    }


    public void setFacets(Hashtable facets, String derivationBy) throws UnknownFacetException,
    IllegalFacetException, IllegalFacetValueException, ConstrainException 
    {
        if( derivationBy.equals( SchemaSymbols.ATTVAL_RESTRICTION )){
            for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();

                if ( key.equals(SchemaSymbols.ELT_LENGTH) ) {
                    _facetsDefined += DatatypeValidator.FACET_LENGTH;
                    String lengthValue = (String)facets.get(key);
                    try {
                        _length     = Integer.parseInt( lengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new IllegalFacetValueException("Length value '"+lengthValue+"' is invalid.");
                    }
                    if ( _length < 0 )
                        throw new IllegalFacetValueException("Length value '"+lengthValue+"'  must be a nonNegativeInteger.");

                } else if (key.equals(SchemaSymbols.ELT_MINLENGTH) ) {
                    _facetsDefined += DatatypeValidator.FACET_MINLENGTH;
                    String minLengthValue = (String)facets.get(key);
                    try {
                        _minLength     = Integer.parseInt( minLengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new IllegalFacetValueException("maxLength value '"+minLengthValue+"' is invalid.");
                    }
                } else if (key.equals(SchemaSymbols.ELT_MAXLENGTH) ) {
                    _facetsDefined += DatatypeValidator.FACET_MAXLENGTH;
                    String maxLengthValue = (String)facets.get(key);
                    try {
                        _maxLength     = Integer.parseInt( maxLengthValue );
                    } catch (NumberFormatException nfe) {
                        throw new IllegalFacetValueException("maxLength value '"+maxLengthValue+"' is invalid.");
                    }
                } else if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                    _facetsDefined += DatatypeValidator.FACET_PATTERN;
                    _pattern = (String)facets.get(key);
                } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                    _facetsDefined += DatatypeValidator.FACET_ENUMERATION;
                    _enumeration = (Vector)facets.get(key);
                } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                    _facetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                    _maxInclusive = (String)facets.get(key);
                } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                    _facetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                    _maxExclusive = (String)facets.get(key);
                } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                    _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                    _minInclusive = (String)facets.get(key);
                } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                    _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                    _minExclusive = (String)facets.get(key);
                } else {
                    throw new IllegalFacetException();
                }
            }

            if (((_facetsDefined & DatatypeValidator.FACET_LENGTH ) != 0 ) ) {
                if (((_facetsDefined & DatatypeValidator.FACET_MAXLENGTH ) != 0 ) ) {
                    throw new ConstrainException(
                                                "It is an error for both length and maxLength to be members of facets." );  
                } else if (((_facetsDefined & DatatypeValidator.FACET_MINLENGTH ) != 0 ) ) {
                    throw new ConstrainException(
                                                "It is an error for both length and minLength to be members of facets." );
                }
            }

            if ( ( (_facetsDefined & ( DatatypeValidator.FACET_MINLENGTH |
                                       DatatypeValidator.FACET_MAXLENGTH) ) != 0 ) ) {
                if ( _minLength < _maxLength ) {
                    throw new ConstrainException( "Value of minLength = " + _minLength +
                                                  "must be greater that the value of maxLength" + _maxLength );
                }
            }

            isMaxExclusiveDefined = ((_facetsDefined & 
                                      DatatypeValidator.FACET_MAXEXCLUSIVE ) != 0 )?true:false;
            isMaxInclusiveDefined = ((_facetsDefined & 
                                      DatatypeValidator.FACET_MAXINCLUSIVE ) != 0 )?true:false;
            isMinExclusiveDefined = ((_facetsDefined &
                                      DatatypeValidator.FACET_MINEXCLUSIVE ) != 0 )?true:false;
            isMinInclusiveDefined = ((_facetsDefined &
                                      DatatypeValidator.FACET_MININCLUSIVE ) != 0 )?true:false;

            if ( isMaxExclusiveDefined && isMaxInclusiveDefined ) {
                throw new ConstrainException(
                                            "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." ); 
            }
            if ( isMinExclusiveDefined && isMinInclusiveDefined ) {
                throw new ConstrainException(
                                            "It is an error for both minInclusive and minExclusive to be specified for the same datatype." ); 
            }
      } else { //derived by list



      }
    }

    public void setBasetype( String base) {
        fBaseValidator = base;
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    public int compare( DatatypeValidator o1, DatatypeValidator o2){
        return 0;
    }

    private void checkContent( String content )throws InvalidDatatypeValueException
    {
        if ( (_facetsDefined & DatatypeValidator.FACET_MAXLENGTH) != 0 ) {
            if ( content.length() > _maxLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+content.length()+
                                                        "' exceeds maximum length of "+_maxLength+".");
            }
        }
        if ( (_facetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
            if ( _enumeration.contains( content ) == false )
                throw new InvalidDatatypeValueException("Value '"+content+"' must be one of "+_enumeration);
        }

        if ( isMaxExclusiveDefined == true ) {
            int comparisonResult;
            comparisonResult  = compare( content, _maxExclusive );
            if ( comparisonResult > 0 ) {
                throw new InvalidDatatypeValueException( "Value '"+content+ "'  must be" +
                                                         "lexicographically less than" + _maxExclusive );

            }

        }
        if ( isMaxInclusiveDefined == true ) {
            int comparisonResult;
            comparisonResult  = compare( content, _maxInclusive );
            if ( comparisonResult >= 0 )
                throw new InvalidDatatypeValueException( "Value '"+content+ "' must be" +
                                                         "lexicographically less or equal than" + _maxInclusive );
        }

        if ( isMinExclusiveDefined == true ) {
            int comparisonResult;
            comparisonResult  = compare( content, _minExclusive );
            if ( comparisonResult < 0 )
                throw new InvalidDatatypeValueException( "Value '"+content+ "' must be" +
                                                         "lexicographically greater than" + _minExclusive );
        }
        if ( isMinInclusiveDefined == true ) {
            int comparisonResult;
            comparisonResult = compare( content, _minInclusive );
            if ( comparisonResult <= 0 )
                throw new InvalidDatatypeValueException( "Value '"+content+ "' must be" +
                                                         "lexicographically greater or equal than" + _minInclusive );
        }

        if ( (_facetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
            RegularExpression regex = new RegularExpression(_pattern, "X" );
            if ( regex.matches( content) == false )
                throw new InvalidDatatypeValueException("Value'"+content+
                                                        "does not match regular expression facet" + _pattern );
        }

    }
    private void checkContentList( String content )throws InvalidDatatypeValueException
    {
        //Revisit
    }
    private int compare( String content, String facetValue ){
        Locale    loc       = Locale.getDefault();
        Collator  collator  = Collator.getInstance( loc );
        return collator.compare( content, facetValue );
    }


}

