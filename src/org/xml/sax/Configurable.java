// Configurable.java - interface for a configurable SAX2 parser.
// $Id$

package org.xml.sax;


/**
 * Interface for a configurable SAX2 parser.
 *
 * This interface allows the application to set or query features and
 * property values from a SAX2 parser.  It provides a standard interface
 * feature negotiation and for extensibility.  Features and properties
 * have unique identifiers, which must be fully-qualified URIs.
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 * @see org.xml.sax.Parser
 * @see org.xml.sax.helpers.ConfigurableParserAdapter
 */
public interface Configurable
{

    /**
     * Set the state of a feature.
     *
     * Set the state of any feature in a SAX2 parser.  The parser
     * might not recognize the feature, and if it does recognize
     * it, it might not be able to fulfill the request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state The requested state of the feature (true or false).
     *
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public abstract void setFeature (String featureId, boolean state)
	throws SAXException;


    /**
     * Query the state of a feature.
     *
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public abstract boolean getFeature (String featureId)
	throws SAXException;


    /**
     * Set the value of a property.
     *
     * Set the value of any property in a SAX2 parser.  The parser
     * might not recognize the property, and if it does recognize
     * it, it might not support the requested value.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @param Object The value to which the property is being set.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public abstract void setProperty (String propertyId, Object value)
	throws SAXException;


    /**
     * Query the value of a property.
     *
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     * @see org.xml.sax.Configurable#getProperty
     */
    public abstract Object getProperty (String propertyId)
	throws SAXException;
}

// end of Configurable.java
