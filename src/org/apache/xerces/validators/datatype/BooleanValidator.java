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
import java.util.Enumeration;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.validators.schema.SchemaSymbols;
/**
 *
 * BooleanValidator validates that content satisfies the W3C XML Datatype for Boolean
 *
 * @author Ted Leung 
 * @author Jeffrey Rodriguez
 * @version
 */

public class BooleanValidator implements DatatypeValidator {
    private Locale fLocale        = null;
    private String fBaseValidator = "native";
    private String _pattern       = null;
    private int    _facetsDefined = 0;
    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private static  final String _valueSpace[]  = { "false", "true", "0", "1" };
    private int       _derivedBy       = DatatypeValidator.DERIVED_BY_RESTRICTION;//default

    /**
     * validate that a string matches the boolean datatype
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     * is not valid.
     */

    public void validate(String content) throws InvalidDatatypeValueException {
        if ( _facetsDefined == 0 )// No Facets to validate against
            return;


        if( _derivedBy == DatatypeValidator.DERIVED_BY_RESTRICTION  ){ 

            ;// What does it mean?
        } else {
            checkContent( content );
        }
    }


    /**
     * Sets the allowable constraining facets
     * for the datatype.
     * 
     * boolean has the following constraining facets:
     * pattern
     * 
     * @param facets Hashtable containing constraining
     *               information passed from the
     *               DatatypeValidatorRegistry.
     * @exception UnknownFacetException
     * @exception IllegalFacetException
     * @exception IllegalFacetValueException
     * @exception ConstrainException
     */
    public void setFacets(Hashtable facets, String derivationBy) throws UnknownFacetException,
    IllegalFacetException, IllegalFacetValueException, ConstrainException {

        if ( derivationBy.equals( SchemaSymbols.ATTVAL_RESTRICTION ) ) {
           _derivedBy = DatatypeValidator.DERIVED_BY_RESTRICTION;

            for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();

                if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                    _facetsDefined += DatatypeValidator.FACET_PATTERN;
                    _pattern = (String)facets.get(key);
                } else {
                    throw new IllegalFacetException();
                }
            }
        }else { // By List

        }
    }


    /**
     * Sets the base datatype name.
     * 
     * @param base
     */
    public void setBasetype(String base) {
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

    private String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        } catch (Exception e) {
            return "Illegal Errorcode "+minor;
        }
    }

    /**
     * Checks content for validity.
     * 
     * @param content
     * @exception InvalidDatatypeValueException
     */
    private void checkContent( String content )throws InvalidDatatypeValueException {
        boolean  isContentInDomain = false;
        for( int i = 0;i<_valueSpace.length;i++ ){
            if( content.equals(_valueSpace[i] ) )
                isContentInDomain = true;
        }
        if (isContentInDomain == false)
           throw new InvalidDatatypeValueException(
                                                  getErrorString(DatatypeMessageProvider.NotBoolean,
                                                                 DatatypeMessageProvider.MSG_NONE,
                                                                 new Object[] { content}));
        if ( (_facetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
            RegularExpression regex = new RegularExpression(_pattern, "X" );
            if ( regex.matches( content) == false )
                throw new InvalidDatatypeValueException("Value'"+content+
                                                        "does not match regular expression facet" + _pattern );
        }
    }
}
