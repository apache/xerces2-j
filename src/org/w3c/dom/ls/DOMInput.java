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
 *  This interface represents an input source for data. 
 * <p> This interface allows an application to encapsulate information about 
 * an input source in a single object, which may include a public 
 * identifier, a system identifier, a byte stream (possibly with a specified 
 * encoding), a base URI, and/or a character stream. 
 * <p> The exact definitions of a byte stream and a character stream are 
 * binding dependent. 
 * <p> The application is expected to provide objects that implement this 
 * interface whenever such objects are needed. The application can either 
 * provide its own objects that implement this interface, or it can use the 
 * generic factory method <code>DOMImplementationLS.createDOMInput()</code> 
 * to create objects that implement this interface. 
 * <p> The <code>DOMParser</code> will use the <code>DOMInput</code> object to 
 * determine how to read data. The <code>DOMParser</code> will look at the 
 * different inputs specified in the <code>DOMInput</code> in the following 
 * order to know which one to read from, the first one through which data is 
 * available will be used: 
 * <ol>
 * <li> <code>DOMInput.characterStream</code> 
 * </li>
 * <li> 
 * <code>DOMInput.byteStream</code> 
 * </li>
 * <li> <code>DOMInput.stringData</code> 
 * </li>
 * <li> 
 * <code>DOMInput.systemId</code> 
 * </li>
 * <li> <code>DOMInput.publicId</code> 
 * </li>
 * </ol> 
 * <p> <code>DOMInput</code> objects belong to the application. The DOM 
 * implementation will never modify them (though it may make copies and 
 * modify the copies, if necessary). 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030619'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMInput {
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a stream of 16-bit units. The application must encode the stream 
     * using UTF-16 (defined in [Unicode] and Amendment 1 of [ISO/IEC 10646]). 
     */
    public java.io.Reader getCharacterStream();
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a stream of 16-bit units. The application must encode the stream 
     * using UTF-16 (defined in [Unicode] and Amendment 1 of [ISO/IEC 10646]). 
     */
    public void setCharacterStream(java.io.Reader characterStream);

    /**
     *  An attribute of a language and binding dependent type that represents 
     * a stream of bytes. 
     * <br> If the application knows the character encoding of the byte 
     * stream, it should set the encoding attribute. Setting the encoding in 
     * this way will override any encoding specified in an XML declaration 
     * in the data. 
     */
    public java.io.InputStream getByteStream();
    /**
     *  An attribute of a language and binding dependent type that represents 
     * a stream of bytes. 
     * <br> If the application knows the character encoding of the byte 
     * stream, it should set the encoding attribute. Setting the encoding in 
     * this way will override any encoding specified in an XML declaration 
     * in the data. 
     */
    public void setByteStream(java.io.InputStream byteStream);

    /**
     *  String data to parse. If provided, this will always be treated as a 
     * sequence of 16-bit units (UTF-16 encoded characters). 
     */
    public String getStringData();
    /**
     *  String data to parse. If provided, this will always be treated as a 
     * sequence of 16-bit units (UTF-16 encoded characters). 
     */
    public void setStringData(String stringData);

    /**
     *  The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], for this 
     * input source. The system identifier is optional if there is a byte 
     * stream, a character stream, or string data, but it is still useful to 
     * provide one, since the application will use it to resolve any 
     * relative URI's and can include it in error messages and warnings (the 
     * <code>DOMParser</code> will only attempt to fetch the resource 
     * identified by the URI reference only if there is no other input 
     * available in the input source). 
     * <br> If the application knows the character encoding of the object 
     * pointed to by the system identifier, it can set the encoding using 
     * the <code>encoding</code> attribute. 
     * <br> If the system ID is a relative URI reference (see section 5 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]), the DOM 
     * implementation will attempt to resolve the relative URI with the 
     * <code>baseURI</code> as the base, if that fails, the behavior is 
     * implementation dependent. 
     */
    public String getSystemId();
    /**
     *  The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], for this 
     * input source. The system identifier is optional if there is a byte 
     * stream, a character stream, or string data, but it is still useful to 
     * provide one, since the application will use it to resolve any 
     * relative URI's and can include it in error messages and warnings (the 
     * <code>DOMParser</code> will only attempt to fetch the resource 
     * identified by the URI reference only if there is no other input 
     * available in the input source). 
     * <br> If the application knows the character encoding of the object 
     * pointed to by the system identifier, it can set the encoding using 
     * the <code>encoding</code> attribute. 
     * <br> If the system ID is a relative URI reference (see section 5 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]), the DOM 
     * implementation will attempt to resolve the relative URI with the 
     * <code>baseURI</code> as the base, if that fails, the behavior is 
     * implementation dependent. 
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

    /**
     *  The public identifier for this input source. This may be mapped to an 
     * input source using an implementation dependent mechanism (such as 
     * catalogues or other mappings). The public identifier, if specified, 
     * may also be reported as part of the location information when errors 
     * are reported. 
     */
    public String getPublicId();
    /**
     *  The public identifier for this input source. This may be mapped to an 
     * input source using an implementation dependent mechanism (such as 
     * catalogues or other mappings). The public identifier, if specified, 
     * may also be reported as part of the location information when errors 
     * are reported. 
     */
    public void setPublicId(String publicId);

    /**
     *  The base URI to be used (see section 5.1.4 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]) for 
     * resolving a relative <code>systemId</code> to an absolute URI. 
     * <br> If, when used, the base URI is itself a relative URI, an empty 
     * string, or null, the behavior is implementation dependent. 
     */
    public String getBaseURI();
    /**
     *  The base URI to be used (see section 5.1.4 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]) for 
     * resolving a relative <code>systemId</code> to an absolute URI. 
     * <br> If, when used, the base URI is itself a relative URI, an empty 
     * string, or null, the behavior is implementation dependent. 
     */
    public void setBaseURI(String baseURI);

    /**
     *  If set to true, assume that the input is certified (see section 2.13 
     * in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]) when 
     * parsing [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]. 
     */
    public boolean getCertified();
    /**
     *  If set to true, assume that the input is certified (see section 2.13 
     * in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]) when 
     * parsing [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]. 
     */
    public void setCertified(boolean certified);

}
