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

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;


/**
 * Provides notification of information which DOM permits to be exposed.
 * This consists of lexical information (comments; and alternative
 * representations and modularizations of content) which is generally
 * intended to be relevant only to document authors.
 *
 * <P> Even when combined with other information provided by SAX
 * parsers, this information is not sufficient to physically recreate
 * the source document.  Certain classes of white space are not
 * presented (e.g. whitespace separating attributes in start tags,
 * and outside of the document element), and line ending characters
 * are normalized (per the XML specification) to a single newline.
 *
 * @see DtdEventListener
 *
 * @author David Brownell (from xml-dev discussions)
 * @version $Revision$
 */
public interface LexicalEventListener extends DocumentHandler
{
    /**
     * Receive notification that the named entity is being included
     * in document content (not element attributes).
     * The name identifies either an internal or external entity,
     * as reported to the DtdEventListener.  <em>Note that the XML
     * specification defines two types of parsed entities:   general
     * entities, and parameter entities.  This event refers only to
     * general parsed entities.</em>
     *
     * @see DtdEventListener
     * @see #endParsedEntity
     *
     * @param name the name of the entity now being included
     * @exception SAXException any SAX exception, possibly wrapping
     *	another exception
     */
    public void startParsedEntity (String name)
    throws SAXException;


    /**
     * Receive notification that the named entity inclusion into document
     * content is completed.  The name identifies either an internal or
     * external entity, as reported to the DtdEventListener.
     *
     * <p> The XML specification requires nonvalidating XML processors to
     * tell applications when they recognize, but do not include, external
     * parsed entities.  (Section 4.4.3 defines entity inclusion in this
     * manner.)  That notification is provided through this method.
     * <em>Applications might request, through EntityResolver, that an
     * entity not be included ... such a mechanism remains TBS.</em>
     *
     * @see #startParsedEntity
     *
     * @param name the name of the entity whose inclusion is completed.
     * @param included true iff the entity was included; always true
     *	for validating parsers.
     * @exception SAXException any SAX exception, possibly wrapping
     *	another exception
     */
    public void endParsedEntity (String name, boolean included)
    throws SAXException;


    /**
     * Receive notification that a CDATA section is beginning.  Data in a
     * CDATA section is is reported through the appropriate event, either
     * <em>characters()</em> or <em>ignorableWhitespace</em>.
     *
     * @see #endCDATA
     * @see org.xml.sax.DocumentHandler#characters
     * @see org.xml.sax.DocumentHandler#ignorableWhitespace
     *
     * @exception SAXException any SAX exception, possibly wrapping
     *	another exception
     */
    public void startCDATA () throws SAXException;


    /**
     * Receive notification that the CDATA section finished.
     *
     * @see #startCDATA
     *
     * @exception SAXException any SAX exception, possibly wrapping
     *	another exception
     */
    public void endCDATA () throws SAXException;


    /**
     * Receive notification that a comment has been read.
     *
     * <P> Note that processing instructions are the mechanism designed
     * to hold information for consumption by applications, not comments.
     * XML systems may rely on applications being able to access information
     * found in processing instructions; this is not true of comments, which
     * are typically discarded.
     *
     * @param text the text within the comment delimiters.
     * @exception SAXException any SAX exception, possibly wrapping
     *	another exception
     */
    public void comment (String text) throws SAXException;
}
