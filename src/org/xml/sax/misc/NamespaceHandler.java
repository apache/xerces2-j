// NamespaceHandler.java - optional handler for namespace scope events.
// $Id$

package org.xml.sax.misc;

import org.xml.sax.SAXException;

/**
 * Optional add-on SAX2 handler for Namespace declarations.
 *
 * This handler type is optional: not all SAX parsers will support
 * it.  To set the NamespaceHandler for a parser, use 
 * Configurable.setProperty with the propertyId
 * "http://xml.org/sax/handlers/NamespaceHandler".
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 * @see org.xml.sax.Configurable
 */
public interface NamespaceHandler
{

    /**
     * Report the start of the scope of a namespace declaration.
     *
     * This event will be reported before the startElement event
     * for the element containing the namespace declaration.  All
     * declarations must be properly nested; if there are multiple
     * declarations in a single element, they must end in the opposite
     * order that they began.
     *
     * @param prefix The declared prefix ("" for the default prefix).
     * @param uri The associated namespace URI ("" to cancel the
     *        scope for a subbranch).
     * @exception SAXException The application may throw an exception.
     * @see #endNamespaceDeclScope
     * @see org.xml.sax.DocumentHandler#startElement
     */
    public abstract void startNamespaceDeclScope (String prefix, String uri)
	throws SAXException;


    /**
     * Report the end of the scope of a namespace declaration.
     *
     * This event will be reported after the endElement event for
     * the element containing the namespace declaration.  Namespace
     * scopes must be properly nested.
     *
     * @param prefix The declared prefix ("" for the default prefix).
     * @exception SAXException The application may throw an exception.
     * @see #startNamespaceDeclScope
     * @see org.xml.sax.DocumentHandler#endElement
     */
    public abstract void endNamespaceDeclScope (String prefix)
	throws SAXException;
}

// end of NamespaceHandler.java
