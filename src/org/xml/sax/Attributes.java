// Attributes.java - attribute list with Namespace support
// Written by David Megginson, sax@megginson.com
// NO WARRANTY!  This class is in the public domain.

// $Id: Attributes.java,v 1.2 2000/01/21 15:13:13 david Exp $


package org.xml.sax;


/**
 * Interface for a list of XML attributes.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * </blockquote>
 *
 * <p>This interface allows access to a list of attributes in
 * three different ways:</p>
 *
 * <ol>
 * <li>by attribute index;</li>
 * <li>by Namespace-qualified name; or</li>
 * <li>by raw XML 1.0 name.</li>
 * </ol>
 *
 * <p>The list will not contain attributes that were declared
 * #IMPLIED but not specified in the start tag.  It will also not
 * contain attributes used as Namespace declarations (xmlns*) unless
 * the http://xml.org/sax/features/raw-names feature is set to
 * true (it is false by default).</p>
 *
 * <p>If the raw-names feature is false, access by raw XML 1.0
 * may not be available; if the http://xml.org/sax/features/namespaces
 * feature is false, access by Namespace-qualified names may not
 * be available.</p>
 *
 * <p>This interface replaces the now-deprecated SAX1 AttributeList
 * interface, which does not contain Namespace support.  In addition
 * to Namespace support, it adds the getIndex methods.</p>
 *
 * <p>The order of attributes in the list is random.</p>
 *
 * @since SAX 2.0
 * @author David Megginson, 
 *         <a href="mailto:sax@megginson.com">sax@megginson.com</a>
 * @version 2.0beta
 * @see org.xml.sax.helpers.AttributeListImpl
 */
public interface Attributes
{


    ////////////////////////////////////////////////////////////////////
    // Indexed access.
    ////////////////////////////////////////////////////////////////////


    /**
     * Return the number of attributes in the list.
     *
     * @return The number of attributes in the list.
     */
    public abstract int getLength ();


    /**
     * Look up an attribute's Namespace URI by index.
     *
     * @param index The attribute index (zero-based).
     * @return The Namespace URI, or the empty string if none
     *         is available, or null if the index is out of
     *         range.
     */
    public abstract String getURI (int index);


    /**
     * Look up an attribute's local name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The local name, or the empty string if Namespace
     *         processing is not being performed, or null
     *         if the index is out of range.
     */
    public abstract String getLocalName (int index);


    /**
     * Look up an attribute's raw XML 1.0 name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The raw XML 1.0 name, or the empty string
     *         if none is available, or null if the index
     *         is out of range.
     */
    public abstract String getRawName (int index);


    /**
     * Look up an attribute's type by index.
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's type as a string, or null if the
     *         index is out of range.
     */
    public abstract String getType (int index);


    /**
     * Look up an attribute's value by index.
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's value as a string, or null if the
     *         index is out of range.
     */
    public abstract String getValue (int index);



    ////////////////////////////////////////////////////////////////////
    // Name-based query.
    ////////////////////////////////////////////////////////////////////


    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri The Namespace URI, or the empty string if
     *        the name has no Namespace URI.
     * @param localName The attribute's local name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex (String uri, String localPart);


    /**
     * Look up the index of an attribute by raw XML 1.0 name.
     *
     * @param rawName The raw (prefixed) name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex (String rawName);


    /**
     * Look up an attribute's type by Namespace name.
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if Namespace
     *         processing is not being performed.
     */
    public abstract String getType (String uri, String localName);


    /**
     * Look up an attribute's type by raw XML 1.0 name.
     *
     * @param rawName The raw XML 1.0 name.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if raw names
     *         are not available.
     */
    public abstract String getType (String rawName);


    /**
     * Look up an attribute's value by Namespace name.
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list.
     */
    public abstract String getValue (String uri, String localName);


    /**
     * Look up an attribute's value by raw XML 1.0 name.
     *
     * @param rawName The raw XML 1.0 name.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list or if raw names
     *         are not available.
     */
    public abstract String getValue (String rawName);

}

// end of Attributes.java
