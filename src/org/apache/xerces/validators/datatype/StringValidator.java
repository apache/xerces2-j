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
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;

/**
 *
 * StringValidator validates that XML content is a W3C string type.
 *
 * @author Ted Leung
 * @version
 */

public class StringValidator implements DatatypeValidator {
	
    private Locale fLocale = null;
    Hashtable facetData = null;
    StringValidator fBaseValidator = null;
    int fMaxLength = 0;
    boolean fIsMaxLength = false;
	
	/**
     * validate that a string is a W3C string type
     *
     * validate returns true or false depending on whether the string content is an
     * instance of the W3C string datatype
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  not a W3C string type
     */

	public void validate(String content, boolean list) throws InvalidDatatypeValueException {

        if (facetData == null)return;

        Enumeration eee = facetData.keys();
        while(eee.hasMoreElements()) {
            String key = (String)eee.nextElement();
            if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                Vector value = (Vector)facetData.get(key);
                String vvv = value.toString();
                if (!value.contains(content)) 
                    throw new InvalidDatatypeValueException("Value '"+content+"' must be one of "+vvv);
            }
            else if (key.equals(SchemaSymbols.ELT_MAXLENGTH)) {
                if (fIsMaxLength && content.length() > fMaxLength)
                    throw new InvalidDatatypeValueException("Value '"+content+"' with length '"+content.length()+"' exceeds maximum length of "+fMaxLength+".");
            }
        }
	}
			
	public void validate(int contentIndex) throws InvalidDatatypeValueException {
	}
	
	public void setFacets(Hashtable facets) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
	    facetData = new Hashtable(); // reset old facets
        for (Enumeration e = facets.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (key.equals(SchemaSymbols.ELT_LENGTH)) {
            } else if (key.equals(SchemaSymbols.ELT_MINLENGTH)) {
            } else if (key.equals(SchemaSymbols.ELT_MAXLENGTH)) {
                int vvv;
                String value = (String)facetData.get(key);
                try {
                    vvv = Integer.parseInt(value);
                } catch(NumberFormatException nfe) {
                    throw new IllegalFacetValueException("maxLength value '"+value+"' is invalid.");
                }
                fMaxLength = vvv;
                fIsMaxLength = true;
            } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
            } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
            } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
            } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
            } else if (key.equals(SchemaSymbols.ELT_PATTERN)) {
            } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                facetData.put(key,facets.get(key));
            } else {
                throw new IllegalFacetException();
            }
        }
	}
	
	public void setFacets(int facets[]) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
	}

	public void setBasetype( String base) {
        //fBaseValidator = (StringValidator) base;
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
}
