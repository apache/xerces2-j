// ConfigurableParserAdapter.java - adapt a SAX 1.0 Parser to Configurable.
// $Id$

package org.xml.sax.helpers;

import java.io.IOException;
import java.util.Locale;

import org.xml.sax.Configurable;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;

import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;


/**
 * Adapt a SAX1 parser to implement the Configurable interface.
 *
 * This class implements the SAX1 Parser interface and the
 * SAX2 Configurable interface by embedding another Parser
 * instance. If the embedded parser already implements the 
 * Configurable interface, it will be used directly; otherwise, 
 * the parser will not know about any features or properties, 
 * and will throw a SAXNotRecognizedException for all of them.
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 * @see org.xml.sax.Parser
 * @see org.xml.sax.Configurable
 */
public class ConfigurableParserAdapter
    implements Parser, Configurable
{

    ////////////////////////////////////////////////////////////////////
    // Constructor.
    ////////////////////////////////////////////////////////////////////

    /**
     * Construct a SAX2 parser by embedding another SAX parser.
     *
     * If the embedded parser does not know about the Configurable
     * interface, this class will implement the interface for it.
     *
     * @param parser The embedded SAX parser.
     */
    public ConfigurableParserAdapter (Parser parser)
    {
	super();
	this.parser = parser;
	if (parser instanceof org.xml.sax.Configurable) {
	    configurable = (Configurable)parser;
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Parser.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the locale for error reporting.
     *
     * Pass on to the embedded parser.
     *
     * @param The locale for error reporting.
     * @exception org.xml.sax.SAXException If the locale is not supported.
     * @see org.xml.sax.Parser#setLocale
     */
    public void setLocale (Locale locale)
	throws SAXException
    {
	parser.setLocale(locale);
    }


    /**
     * Set the parser's entity resolver.
     *
     * Pass on to the embedded parser.
     *
     * @param resolver The application-provided entity resolver.
     * @see org.xml.sax.Parser#setEntityResolver
     */
    public void setEntityResolver (EntityResolver resolver)
    {
	parser.setEntityResolver(resolver);
    }


    /**
     * Set the parser's DTD event handler.
     *
     * Pass on to the embedded parser.
     *
     * @param handler The application-provided DTD event handler.
     * @see org.xml.sax.Parser#setDTDHandler
     */
    public void setDTDHandler (DTDHandler handler)
    {
	parser.setDTDHandler(handler);
    }


    /**
     * Set the parser's document event handler.
     *
     * Pass on to the embedded parser.
     *
     * @param handler The application-provided document event handler.
     * @see org.xml.sax.Parser#setDocumentHandler
     */
    public void setDocumentHandler (DocumentHandler handler)
    {
	parser.setDocumentHandler(handler);
    }


    /**
     * Set the parser's error event handler.
     *
     * Pass on to the embedded parser.
     *
     * @param handler The application-provided error event handler.
     * @see org.xml.sax.Parser#setErrorHandler
     */
    public void setErrorHandler (ErrorHandler handler)
    {
	parser.setErrorHandler(handler);
    }


    /**
     * Parse an XML document from an InputSource.
     *
     * Pass on to the embedded parser.
     *
     * @param source The InputSource for the XML document.
     * @see org.xml.sax.Parser#parse(org.xml.sax.InputSource)
     */
    public void parse (InputSource source)
	throws SAXException, IOException
    {
	parser.parse(source);
    }


    /**
     * Parse an XML document from a fully-qualified system ID.
     *
     * Pass on to the embedded parser.
     *
     * @param systemId The system identifier (URI) for the document.
     * @see org.xml.sax.Parser#parse(java.lang.String)
     */
    public void parse (String systemId)
	throws SAXException, IOException
    {
	parser.parse(systemId);
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Configurable.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the state of a feature.
     *
     * Always fails unless the embedded parser supports Configurable.
     *
     * @see org.xml.sax.Configurable#setFeature
     */
    public void setFeature (String featureId, boolean state)
	throws SAXException
    {
	if (configurable == null) {
	    throw new SAXNotRecognizedException("Feature: " + featureId);
	} else {
	    configurable.setFeature(featureId, state);
	}
    }


    /**
     * Get the state of a feature.
     *
     * Always fails unless the embedded parser supports Configurable.
     *
     * @see org.xml.sax.Configurable#getFeature
     */
    public boolean getFeature (String featureId)
	throws SAXException
    {
	if (configurable == null) {
	    throw new SAXNotRecognizedException("Feature: " + featureId);
	} else {
	    return configurable.getFeature(featureId);
	}
    }


    /**
     * Set the state of a property.
     *
     * Always fails unless the embedded parser supports Configurable.
     *
     * @see org.xml.sax.Configurable#setProperty
     */
    public void setProperty (String propertyId, Object value)
	throws SAXException
    {
	if (configurable == null) {
	    throw new SAXNotRecognizedException("Property: " + propertyId);
	} else {
	    configurable.setProperty(propertyId, value);
	}
    }


    /**
     * Get the state of a property.
     *
     * Always fails unless the embedded parser supports Configurable.
     *
     * @see org.xml.sax.Configurable#getProperty
     */
    public Object getProperty (String propertyId)
	throws SAXException
    {
	if (configurable == null) {
	    throw new SAXNotRecognizedException("Property: " + propertyId);
	} else {
	    return configurable.getProperty(propertyId);
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    private Parser parser = null;
    private Configurable configurable = null;
}

// end of ConfigurableParserAdapter.java
