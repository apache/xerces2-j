// SAXNotRecognizedException.java - unrecognized feature or value.
// $Id: SAXNotRecognizedException.java,v 1.1.1.1 1999/08/26 15:51:09 twl Exp $

package org.xml.sax;

/**
 * Exception class for an unrecognized identifier.
 *
 * A SAX2 parser that implements the Configurable interface
 * will throw this exception when it finds an unrecognized
 * feature or property identifier; SAX applications and extensions
 * may use this class for other, similar purposes.
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 */
public class SAXNotRecognizedException extends SAXException
{
    /**
     * Construct a new exception with the given message.
     *
     * @param message The text message of the exception.
     */
    public SAXNotRecognizedException (String message)
    {
	super(message);
    }
}

// end of SAXNotRecognizedException.java
