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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;

/**
 *
 * StringValidator validates that XML content is a W3C string type.
 *
 */

public class StringValidator implements DatatypeValidator {
    private Locale    fLocale          = null;
    private String    fBaseValidator   = "native";

    private int       _length          = 0;
    private int       _maxLength       = 0;
    private int       _minLength       = 0;
    private String    _pattern         = null;
    private Vector    _enumeration     = null;
    private int       _maxInclusive    = 0;
    private int       _maxExclusive    = 0;
    private int       _minInclusive    = 0;
    private int       _minExclusive    = 0;
    private int       _facetsDefined   = 0;

    /**
     * validate that a string is a W3C string type
     * 
     * @param content A string containing the content to be validated
     * @param list
     * @exception throws InvalidDatatypeException if the content is
     *                   not a W3C string type
     * @exception InvalidDatatypeValueException
     */
    public void validate(String content, boolean derivedByList)  throws InvalidDatatypeValueException
    {
        if ( _facetsDefined == 0 )// No Facets to validate against
            return;

        StringTokenizer parsedList = null;
        if ( derivedByList == true ) {
            parsedList = new StringTokenizer( content );
            try {
                while ( parsedList.hasMoreTokens() ) {
                    checkContent( parsedList.nextToken() );
                }
            } catch ( NoSuchElementException e ) {
                e.printStackTrace();
            }
        } else {
            checkContent( content );
        }
    }


    public void setFacets(Hashtable facets) throws UnknownFacetException,
    IllegalFacetException, IllegalFacetValueException 
    {
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
                String maxInclusiveValue = (String)facets.get(key);
                try {
                    _maxInclusive   = Integer.parseInt( maxInclusiveValue );
                } catch (NumberFormatException nfe) {
                    throw new IllegalFacetValueException("maxLength value '"+maxInclusiveValue+"' is invalid.");
                }
            } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                _facetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                String maxExclusiveValue = (String)facets.get(key);
                try {
                    _maxExclusive   = Integer.parseInt( maxExclusiveValue );
                } catch (NumberFormatException nfe) {
                    throw new IllegalFacetValueException("maxLength value '"+maxExclusiveValue+"' is invalid.");
                }
            } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                String minInclusiveValue = (String)facets.get(key);
                try {
                    _minInclusive   = Integer.parseInt( minInclusiveValue );
                } catch (NumberFormatException nfe) {
                    throw new IllegalFacetValueException("maxLength value '"+minInclusiveValue+"' is invalid.");
                }
            } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                String minExclusiveValue = (String)facets.get(key);
                try {
                    _minExclusive   = Integer.parseInt( minExclusiveValue );
                } catch (NumberFormatException nfe) {
                    throw new IllegalFacetValueException("maxLength value '"+minExclusiveValue+"' is invalid.");
                }
            } else {
                throw new IllegalFacetException();
            }
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
        if ( (_facetsDefined & DatatypeValidator.FACET_MAXLENGTH) == 1 ) {
            if ( content.length() > _maxLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                                                        "' with length '"+content.length()+
                                                        "' exceeds maximum length of "+_maxLength+".");
            }
        }
        if ( (_facetsDefined & DatatypeValidator.FACET_ENUMERATION) == 1 ) {
            if ( !_enumeration.contains( content ) )
                throw new InvalidDatatypeValueException("Value '"+content+"' must be one of "+_enumeration);
        }
    }
}

