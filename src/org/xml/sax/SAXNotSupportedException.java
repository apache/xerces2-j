// SAXNotSupportedException.java - unsupported feature or value.
// $Id: SAXNotSupportedException.java,v 1.1.1.1 1999/08/26 15:51:09 twl Exp $

package org.xml.sax;

/**
 * Exception class for an unsupported operation.
 *
 * A SAX2 parser that implements the Configurable interface
 * will throw this exception when it recognizes a
 * feature or property identifier, but cannot perform the
 * requested operation (setting a state or value).  Other
 * SAX2 applications and extentions may use this class
 * for similar purposes.
 *
 * @author David Megginson &lt;david@megginson.com&gt;
 * @version
 */
public class SAXNotSupportedException extends SAXException
{

    /**
     * Construct a new exception with the given message.
     *
     * @param message The text message of the exception.
     */
    public SAXNotSupportedException (String message)
    {
	super(message);
    }

}

// end of SAXNotSupportedException.java
