// LexicalHandler.java - optional handler for lexical parse events.
// $Id$

package org.xml.sax.misc;

import org.xml.sax.SAXException;

/**
 * Optional add-on SAX2 handler for lexical events.
 *
 * This handler type is optional: not all SAX parsers will support
 * it.  To set the LexicalHandler for a parser, use Configurable.setProperty
 * with the propertyId "http://xml.org/sax/handlers/LexicalHandler".
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 * @see org.xml.sax.Configurable
 */
public interface LexicalHandler
{

    /**
     * Report the start of DTD declarations, if any.
     *
     * Any declarations are assumed to be in the internal subset
     * unless otherwise indicated.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the
     *        external DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the
     *        external DTD subset, or null if none was declared.
     * @exception SAXException The application may raise an
     *            exception.
     * @see #endDTD
     * @see #startEntity
     */
    public abstract void startDTD (String name, String publicId,
				   String systemId)
	throws SAXException;


    /**
     * Report the end of DTD declarations.
     *
     * @exception SAXException The application may raise an exception.
     * @see #startDTD
     */
    public abstract void endDTD ()
	throws SAXException;


    /**
     * Report the beginning of an entity.
     *
     * The start and end of the document entity are not reported.
     * The start and end of the external DTD subset are reported
     * using the pseudo-name "[dtd]".  All other events must be
     * properly nested within start/end entity events.
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @exception SAXException The application may raise an exception.
     * @see #endEntity
     * @see org.xml.sax.misc.DeclHandler#internalEntityDecl
     * @see org.xml.sax.misc.DeclHandler#externalEntityDecl
     */
    public abstract void startEntity (String name)
	throws SAXException;


    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     * @exception SAXException The application may raise an exception.
     * @see #startEntity
     */
    public abstract void endEntity (String name)
	throws SAXException;


    /**
     * Report the start of a CDATA section.
     *
     * @exception SAXException The application may raise an exception.
     * @see #endCDATA
     */
    public abstract void startCDATA ()
	throws SAXException;


    /**
     * Report the end of a CDATA section.
     *
     * @exception SAXException The application may raise an exception.
     * @see #startCDATA
     */
    public abstract void endCDATA ()
	throws SAXException;


    /**
     * Report an XML comment anywhere in the document.
     *
     * This callback will be used for comments inside or outside the
     * document element, including comments in the external DTD
     * subset (if read).
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception SAXException The application may raise an exception.
     */
    public abstract void comment (char ch[], int start, int length)
	throws SAXException;

}

// end of LexicalHandler.java
