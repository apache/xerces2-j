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

import java.util.EventListener;

import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * When parsing XML documents, DTD related events are signaled through
 * this interface.  Examples include the beginning and end of the DTD,
 * entity declarations, and parts of the &lt;!DOCTYPE...&gt; declaration.
 * This interface supports: <UL>
 *
 * <LI> SAX 1.0 DtdHandler callbacks, which suffice to interpret element
 * attributes whose values name notations or unparsed entities. 
 *
 * <LI>DOM Level 1, which additionally requires information about parsed
 * entity declarations, as well as the ability to tell which processing
 * instructions are within the DTD.
 *
 * <LI> XML namespaces, which places requirements on names of all
 * entities and notations (they may not contain colons.)
 *
 * <LI> Recreating the <em>&lt;!DOCTYPE ...&gt;</em> declaration when
 * storing modified documents, including the unnamed external parameter
 * entity as well as the full text of the internal DTD subset.
 *
 * <LI> Layered validation of element content models and attribute
 * content restrictions, by exposing these declarations for applications
 * to record and use.
 *
 * </UL>
 *
 * <P> Events signaled through the DTDHandler interface will only
 * signaled after at <em>startDtd</em> event and before an <em>endDtd</em>
 * event.
 *
 * <P> Other than the support to recreate the DOCTYPE declaration,
 * parameter entities are not exposed.
 *
 * @see Parser
 *
 * @author David Brownell
 * @version $Revision$
 */
public interface DtdEventListener extends DTDHandler
{
    /**
     * Receive notification of the beginning of a Document Type
     * Declaration (DTD).  The parser will invoke this method only
     * once, before any other methods in this interface.
     *
     * @see #endDtd
     *
     * @param rootName The declared name of the root element, which
     *	may be different from the actual name if the document is not
     *	valid.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void startDtd (String rootName)
    throws SAXException;


    /**
     * Reports the optional unnamed parameter entity which is processed
     * after the internal DTD subset.  This commonly provides the entire
     * external DTD subset, in case where the internal DTD subset does
     * not define and use external parameter entities.  The parser will
     * invoke this method at most once.
     *
     * @see #internalDtdDecl
     *
     * @param publicId The entity's public identifier, or null if
     *        none was given.
     * @param systemId The entity's system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void externalDtdDecl (String publicId, String systemId)
    throws SAXException;


    /**
     * Reports the internal DTD subset, as unparsed markup declarations.
     * This may also contain comments and processing instructions, if they
     * are found in the document source.  Applications should treat this
     * string as opaque, only using it when recreating a
     * <em>&lt;!DOCTYPE ...&gt;</em> declaration.  The parser will invoke
     * this method at most once.
     *
     * @see #externalDtdDecl
     *
     * @param internalSubset The document's internal subset, as unparsed
     *	markup declarations, comments, and processing instructions.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void internalDtdDecl (String internalSubset)
    throws SAXException;


    /**
     * Receive notification of a internal parsed entity declaration event.
     *
     * @see #externalEntityDecl
     *
     * @param name The internal entity name.
     * @param value the value of the entity, which may include unexpanded
     *	entity references.  Character references will have been expanded.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void internalEntityDecl (String name, String value)
    throws SAXException;


    /**
     * Receive notification of an external parsed entity declaration event.
     *
     * <p>If a system identifier is present, and it is a relative URL, the
     * parser will have resolved it fully before passing it through this
     * method to a listener.</p>
     *
     * @see #unparsedEntityDecl
     *
     * @param name The entity name.
     * @param publicId The entity's public identifier, or null if
     *        none was given.
     * @param systemId The entity's system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void externalEntityDecl (String name,
		    String publicId, String systemId)
    throws SAXException;


    // SAX 1.0 defines unparsedEntityDecl

    // SAX 1.0 defines notationDecl


    /**
     * Reports an element declaration found within the DTD.  The content
     * model will be a string such as "ANY", "EMPTY", "(#PCDATA|foo)*",
     * or "(caption?,tr*)".
     *
     * @param elementName The name of the element; this includes a namespace
     *	prefix if one was used within the DTD.
     * @param contentModel The content model as defined in the DTD, with
     *	any whitespace removed.
     */
    public void elementDecl (String elementName, String contentModel)
    throws SAXException;

    /**
     * Reports an attribute declaration found within the DTD.
     *
     * @param elementName The name of the element to which the attribute
     *	applies; this includes a namespace prefix if one was used within
     *	the DTD.
     * @param attributeName The name of the attribute being declared; this
     *	includes a namespace prefix if one was used within the DTD.
     * @param attributeType The type of the attribute, either CDATA, NMTOKEN,
     *	NMTOKENS, ENTITY, ENTITIES, NOTATION, ID, IDREF, or IDREFS as
     *	defined in the XML specification; or null for enumerations.
     * @param options When attributeType is null or NOTATION, this is an
     *	array of the values which are permitted; it is otherwise null.
     * @param defaultValue When not null, this provides the default value
     *	of this attribute.
     * @param isFixed When true, the defaultValue is the only legal value.
     *	(Precludes isRequired.)
     * @param isRequired When true, the attribute value must be provided
     *	for each element of the named type.  (Precludes isFixed.)
     */
    public void attributeDecl (
	String		elementName,
	String		attributeName,
	String		attributeType,
	String		options [],
	String		defaultValue,
	boolean		isFixed,
	boolean		isRequired
    ) throws SAXException;


    /**
     * Receive notification of the end of a DTD.  The parser will invoke
     * this method only once.
     *
     * @see #startDtd
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void endDtd () throws SAXException;
}
