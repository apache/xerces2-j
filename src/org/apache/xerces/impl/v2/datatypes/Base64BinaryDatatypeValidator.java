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

package org.apache.xerces.impl.v2.datatypes;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.xerces.impl.v2.SchemaSymbols;
import org.apache.xerces.impl.v2.util.regex.RegularExpression;
import org.apache.xerces.impl.v2.util.Base64;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.v2.XSMessageFormatter;

import org.apache.xerces.impl.validation.ValidationContext;
/**
 * Base64BinaryValidator validates that XML content is a W3C string type.
 * @author Ted Leung
 * @author Kito D. Mann, Virtua Communications Corp.
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */
public class Base64BinaryDatatypeValidator extends AbstractStringValidator{
    


    public  Base64BinaryDatatypeValidator ()  {
        super( null, null, false , null); // Native, No Facets defined, Restriction

    }

    public Base64BinaryDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                           boolean derivedByList, XMLErrorReporter reporter) {

        super (base, facets, derivedByList, reporter);         
    }

    protected void assignAdditionalFacets(String key, Hashtable facets)  throws InvalidDatatypeFacetException{
        String msg = "base64Binary datatype, facet "+key+" with value "+(String)facets.get(key);
        throw new InvalidDatatypeFacetException(msg);    
    }


    protected void checkValueSpace (String content) throws InvalidDatatypeValueException {
        if (getLength( content) < 0) {
            throw new InvalidDatatypeValueException( "Value '"+content+"' is not encoded in Base64" );
        }
    }

    protected int getLength( String content) {
      int x = 0;
      try {
        x = Base64.getDecodedDataLength(content.getBytes("utf-8"));
      }
      catch (UnsupportedEncodingException e) {
      }
      finally {
        return x;
      }
    }

    public int compare( String value1, String value2 ){
        if (value1 == null || value2 == null)
            return -1;

        if (value1 == value2 || value1.equals(value2))
            return 0;

        byte[] data1=Base64.decode(value1.getBytes());
        byte[] data2=Base64.decode(value2.getBytes());

        if (data1 == null || data2 == null)
            return -1;

        for (int i = 0; i < Math.min(data1.length, data2.length); i++)
            if (data1[i] < data2[i])
                return -1;
            else if (data1[i] > data2[i])
                return 1;

        if (data1.length == data2.length)
            return 0;

        return data1.length > data2.length ? 1 : -1;
    }
}
