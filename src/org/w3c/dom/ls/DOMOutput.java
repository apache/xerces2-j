/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.dom.ls;

/**
 *  This interface represents an output destination for data. 
 * <p> This interface allows an application to encapsulate information about 
 * an output destination in a single object, which may include a URI, a byte 
 * stream (possibly with a specified encoding), a base URI, and/or a 
 * character stream. 
 * <p> The exact definitions of a byte stream and a character stream are 
 * binding dependent. 
 * <p> The application is expected to provide objects that implement this 
 * interface whenever such objects are needed. The application can either 
 * provide its own objects that implement this interface, or it can use the 
 * generic factory method <code>DOMImplementationLS.createDOMOutput()</code> 
 * to create objects that implement this interface. 
 * <p> The <code>DOMSerializer</code> will use the <code>DOMOutput</code> 
 * object to determine where to serialize the output to. The 
 * <code>DOMSerializer</code> will look at the different outputs specified 
 * in the <code>DOMOutput</code> in the following order to know which one to 
 * output to, the first one that data can be output to will be used: 
 * <ol>
 * <li> 
 * <code>DOMOutput.characterStream</code> 
 * </li>
 * <li> <code>DOMOutput.byteStream</code> 
 * </li>
 * <li>
 *  <code>DOMOutput.systemId</code> 
 * </li>
 * </ol> 
 * <p> <code>DOMOutput</code> objects belong to the application. The DOM 
 * implementation will never modify them (though it may make copies and 
 * modify the copies, if necessary). 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030619'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMOutput {
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a writable stream to which 16-bit units can be output. The 
     * application must encode the stream using UTF-16 (defined in [Unicode] and Amendment 1 of [ISO/IEC 10646]). 
     */
    public java.io.Writer getCharacterStream();
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a writable stream to which 16-bit units can be output. The 
     * application must encode the stream using UTF-16 (defined in [Unicode] and Amendment 1 of [ISO/IEC 10646]). 
     */
    public void setCharacterStream(java.io.Writer characterStream);

    /**
     *  An attribute of a language and binding dependent type that represents 
     * a writable stream of bytes. 
     * <br> If the application knows the character encoding of the byte 
     * stream, it should set the encoding attribute. Setting the encoding in 
     * this way will override any encoding specified in an XML declaration 
     * in the data. 
     */
    public java.io.OutputStream getByteStream();
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a writable stream of bytes. 
     * <br> If the application knows the character encoding of the byte 
     * stream, it should set the encoding attribute. Setting the encoding in 
     * this way will override any encoding specified in an XML declaration 
     * in the data. 
     */
    public void setByteStream(java.io.OutputStream byteStream);

    /**
     *  The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], for this 
     * output destination. 
     * <br> If the application knows the character encoding of the object 
     * pointed to by the system identifier, it can set the encoding using 
     * the <code>encoding</code> attribute. 
     * <br> If the system ID is a relative URI reference (see section 5 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]), the 
     * behavior is implementation dependent. 
     */
    public String getSystemId();
    /**
     *  The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], for this 
     * output destination. 
     * <br> If the application knows the character encoding of the object 
     * pointed to by the system identifier, it can set the encoding using 
     * the <code>encoding</code> attribute. 
     * <br> If the system ID is a relative URI reference (see section 5 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]), the 
     * behavior is implementation dependent. 
     */
    public void setSystemId(String systemId);

    /**
     *  The character encoding, if known. The encoding must be a string 
     * acceptable for an XML encoding declaration ([<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>] section 
     * 4.3.3 "Character Encoding in Entities"). 
     * <br> This attribute has no effect when the application provides a 
     * character stream or string data. For other sources of input, an 
     * encoding specified by means of this attribute will override any 
     * encoding specified in the XML declaration or the Text declaration, or 
     * an encoding obtained from a higher level protocol, such as HTTP [<a href='http://www.ietf.org/rfc/rfc2616.txt'>IETF RFC 2616</a>]. 
     */
    public String getEncoding();
    /**
     *  The character encoding, if known. The encoding must be a string 
     * acceptable for an XML encoding declaration ([<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>] section 
     * 4.3.3 "Character Encoding in Entities"). 
     * <br> This attribute has no effect when the application provides a 
     * character stream or string data. For other sources of input, an 
     * encoding specified by means of this attribute will override any 
     * encoding specified in the XML declaration or the Text declaration, or 
     * an encoding obtained from a higher level protocol, such as HTTP [<a href='http://www.ietf.org/rfc/rfc2616.txt'>IETF RFC 2616</a>]. 
     */
    public void setEncoding(String encoding);

}
