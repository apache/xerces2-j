/*
 * Copyright (c) 2001 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.apache.xerces.dom3.ls;

import org.w3c.dom.DOMException;

/**
 *  <code>DOMImplementationLS</code> contains the factory methods for creating 
 * objects that implement the <code>DOMBuilder</code> (parser) and 
 * <code>DOMWriter</code> (serializer) interfaces. 
 * <p> An object that implements DOMImplementationLS is obtained by doing a 
 * binding specific cast from DOMImplementation to DOMImplementationLS. 
 * Implementations supporting the Load and Save feature must implement the 
 * DOMImplementationLS interface on whatever object implements the 
 * DOMImplementation interface. 
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-ASLS-20011025'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 */
public interface DOMImplementationLS {
    // DOMIMplementationLSMode
    /**
     * Create a synchronous <code>DOMBuilder</code>.
     */
    public static final short MODE_SYNCHRONOUS          = 1;
    /**
     * Create an asynchronous <code>DOMBuilder</code>.
     */
    public static final short MODE_ASYNCHRONOUS         = 2;

    /**
     * Create a new <code>DOMBuilder</code>. The newly constructed parser may 
     * then be configured by means of its <code>setFeature</code> method, 
     * and used to parse documents by means of its <code>parse</code> 
     * method. 
     * @param mode  The <code>mode</code> argument is either 
     *   <code>MODE_SYNCHRONOUS</code> or <code>MODE_ASYNCHRONOUS</code>, if 
     *   <code>mode</code> is <code>MODE_SYNCHRONOUS</code> then the 
     *   <code>DOMBuilder</code> that is created will operate in synchronous 
     *   mode, if it's <code>MODE_ASYNCHRONOUS</code> then the 
     *   <code>DOMBuilder</code> that is created will operate in 
     *   asynchronous mode. 
     * @return  The newly created <code>DOMBuilder</code> object, this 
     *   <code>DOMBuilder</code> is either synchronous or asynchronous 
     *   depending on the value of the <code>type</code> argument. 
     * @exception DOMException
     *    Raise a NOT_SUPPORTED_ERR exception if MODE_ASYNCHRONOUS is not 
     *   supported. 
     */
    public DOMBuilder createDOMBuilder(short mode)
                                       throws DOMException;

    /**
     * Create a new <code>DOMWriter</code> object. <code>DOMWriter</code>s are 
     * used to serialize a DOM tree back into an XML document. 
     * @return The newly created <code>DOMWriter</code> object.
     */
    public DOMWriter createDOMWriter();

    /**
     *  Create a new "empty" <code>DOMInputSource</code>. 
     * @return  The newly created <code>DOMBuilder</code> object, this 
     *   <code>DOMBuilder</code> is either synchronous or asynchronous 
     *   depending on the value of the <code>type</code> argument. 
     */
    public DOMInputSource createDOMInputSource();

}
