// DeclHandler.java - Optional handler for DTD declaration events.
// $Id$

package org.xml.sax.misc;

import org.xml.sax.SAXException;


/**
 * Optional SAX2 handler for DTD declaration events.
 *
 * This handler type is optional: not all SAX parsers will support
 * it.  To set the DeclHandler for a parser, use Configurable.setProperty
 * with the propertyId "http://xml.org/sax/handlers/DeclHandler".
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 * @see org.xml.sax.Configurable
 */
public interface DeclHandler
{

    /**
     * Report an element type declaration.
     *
     * The content model will consist of the string "EMPTY", the
     * string "ANY", or a parenthesised group, optionally followed
     * by an occurrence indicator.  The model will be normalized so
     * that all whitespace is removed.
     *
     * @param name The element type name.
     * @param model The content model as a normalized string.
     * @exception SAXException The application may raise an exception.
     */
    public abstract void elementDecl (String name, String model)
	throws SAXException;


    /**
     * Report an attribute type declaration.
     *
     * Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with 
     * the separator "|" and all whitespace removed.
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public abstract void attributeDecl (String eName,
					String aName,
					String type,
					String valueDefault,
					String value)
	throws SAXException;


    /**
     * Report an internal entity declaration.
     *
     * Only the effective (first) declaration for each entity
     * will be reported.
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param value The replacement text of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #externalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public abstract void internalEntityDecl (String name, String value)
	throws SAXException;


    /**
     * Report a parsed external entity declaration.
     *
     * Only the effective (first) declaration for each entity
     * will be reported.
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @param publicId The declared public identifier of the entity, or
     *        null if none was declared.
     * @param systemId The declared system identifier of the entity.
     * @exception SAXException The application may raise an exception.
     * @see #internalEntityDecl
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public abstract void externalEntityDecl (String name, String publicId,
					     String systemId)
	throws SAXException;

}

// end of DeclHandler.java
