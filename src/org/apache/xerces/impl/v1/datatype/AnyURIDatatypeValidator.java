/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.v1.datatype;

import java.util.Hashtable;

import org.apache.xerces.util.URI;

/**
 * URIValidator validates that XML content is a W3C uri type,
 * according to RFC 2396
 *
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @see          RFC 2396
 * @see Tim Berners-Lee, et. al. RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax.. 1998 Available at: http://www.ietf.org/rfc/rfc2396.txt
 * @version  $Id$
 */
public class AnyURIDatatypeValidator extends AbstractStringValidator {
    
    private URI fTempURI = null;
    public AnyURIDatatypeValidator () throws InvalidDatatypeFacetException{
        super ( null, null, false ); // Native, No Facets defined, Restriction
    }

    public AnyURIDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                     boolean derivedByList ) throws InvalidDatatypeFacetException {
        super (base, facets, derivedByList); 
    }

    protected void assignAdditionalFacets(String key, Hashtable facets)  throws InvalidDatatypeFacetException{
        String msg = getErrorString(
            DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.ILLEGAL_STRING_FACET],
            new Object[] { key });
        throw new InvalidDatatypeFacetException(msg);
    }


    protected void checkValueSpace (String content) throws InvalidDatatypeValueException {
        
        // check 3.2.17.c0 must: URI (rfc 2396/2723)
        try {
            if (fTempURI == null) {
                fTempURI = new URI("http://www.template.com");
            }
            if( content.length() != 0 ) {
                // Support for relative URLs
                // According to Java 1.1: URLs may also be specified with a 
                // String and the URL object that it is related to.
                //
                new URI(fTempURI, content );
            }
        } catch (  URI.MalformedURIException ex ) {
                throw new InvalidDatatypeValueException("Value '"+content+"' is a Malformed URI ");
        }
    }

    public int compare( String  content1, String content2){
        // TO BE DONE!!!
        return content1.equals(content2)?0:-1;
    }
}
