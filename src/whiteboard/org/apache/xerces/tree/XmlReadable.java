/*
 * $Id$
 *
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
 * 4. The names "Crimson" and "Apache Software Foundation" must
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
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */

package org.apache.xerces.tree;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 * This interface is supported by XML documents and elements which wish to
 * interact with DOM construction during parsing of XML documents.  The
 * parse context which is provided allows elements to determine the URI of
 * the document in which they are found, for interpreting relative URIs.
 * It also supports providing application level diagnostics for faulty
 * input.
 *
 * <P> When these methods are called, parent context is available for
 * elements so that "inherited" attributes may be queried, as well as
 * other information such as the types of any containing elements.
 *
 * @author David Brownell
 * @version $Revision$
 */
public interface XmlReadable
{
    /**
     * This is called before object children are parsed.  For elements,
     * this is a natural time to perform tasks which relate to element
     * attributes, such as application level integrity checks or
     * associating them with object properties.
     */
    void startParse (ParseContext context)
    throws SAXException;

    /**
     * This is called when each child element has been 
     * fully constructed.  The object may choose to represent the
     * child's information in a manner which is more appropriate to a
     * particular application, or even discard that information if it
     * is not currently needed.  For example, this is a good time for
     * elements discard ignorable whitespace, filter out elements not matching
     * some search criteria, or map certain elements to object properties.
     */
    void doneChild (NodeEx newChild, ParseContext context)
    throws SAXException;

    /**
     * This is called when the object has been fully parsed, sometime after
     * startParse.  It is a natural time to perform tasks which relate to
     * all children, such as verifying application level integrity constraints
     * or associating an appropriate <em>userObject</em> with this element.
     * Documents may wish to perform ID/IDREF link fixup, or similar tasks.
     */
    void doneParse (ParseContext context)
    throws SAXException;
}
