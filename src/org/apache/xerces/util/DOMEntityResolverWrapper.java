/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  
 * All rights reserved.
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.util;


import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import org.apache.xerces.dom3.ls.DOMEntityResolver;
import org.apache.xerces.dom3.ls.DOMInputSource;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;


/**
 * This class wraps DOM entity resolver to XNI entity resolver.
 *
 * @see DOMEntityResolver
 *
 * @author Gopal Sharma, SUN MicroSystems Inc.
 * @author Elena Litani, IBM 
 * 
 */
public class DOMEntityResolverWrapper
    implements XMLEntityResolver {

    //
    // Data
    //

    /** The DOM entity resolver. */
    protected DOMEntityResolver fEntityResolver;

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMEntityResolverWrapper() {}

    /** Wraps the specified DOM entity resolver. */
    public DOMEntityResolverWrapper(DOMEntityResolver entityResolver) {
        setEntityResolver(entityResolver);
    } // DOMEntityResolver

    //
    // Public methods
    //

    /** Sets the DOM entity resolver. */
    public void setEntityResolver(DOMEntityResolver entityResolver) {
        fEntityResolver = entityResolver;
    } // setEntityResolver(DOMEntityResolver)

    /** Returns the DOM entity resolver. */
    public DOMEntityResolver getEntityResolver() {
        return fEntityResolver;
    } // getEntityResolver():DOMEntityResolver

    //
    // XMLEntityResolver methods
    //
    
    /**
     * Resolves an external parsed entity. If the entity cannot be
     * resolved, this method should return null.
     *
     * @param resourceIdentifier	description of the resource to be revsoved
     * @throws XNIException Thrown on general error.
     * @throws IOException  Thrown if resolved entity stream cannot be
     *                      opened or some other i/o error occurs.
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException {

        // resolve entity using DOM entity resolver
        if (fEntityResolver != null) {
            try {
                DOMInputSource inputSource = 
		    resourceIdentifier == null ?
                    fEntityResolver.resolveEntity(null, null, null) : 
                    fEntityResolver.resolveEntity(resourceIdentifier.getPublicId(), resourceIdentifier.getLiteralSystemId(), resourceIdentifier.getBaseSystemId());
                if (inputSource != null) {
                    String publicId = inputSource.getPublicId();
                    String systemId = inputSource.getSystemId();
                    String baseSystemId = inputSource.getBaseURI();
                    InputStream byteStream = inputSource.getByteStream();
                    Reader charStream = inputSource.getCharacterStream();
                    String encoding = inputSource.getEncoding();
                    XMLInputSource xmlInputSource =
                        new XMLInputSource(publicId, systemId, baseSystemId);
                    xmlInputSource.setByteStream((InputStream)byteStream);
                    xmlInputSource.setCharacterStream(charStream);
                    xmlInputSource.setEncoding(encoding);
                    return xmlInputSource;
                }
            }

            // error resolving entity
            catch (Exception e) {
                // REVISIT:
                // can at this point we receive wrapped exception?

                throw new XNIException(e);
            }
        }

        // unable to resolve entity
        return null;

    } // resolveEntity(String,String,String):XMLInputSource
    
} // DOMEntityResolverWrapper
